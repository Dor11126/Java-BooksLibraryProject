package com.dor_cohen.network.server;

import com.dor_cohen.network.dto.Request;
import com.dor_cohen.network.dto.Response;
import com.dor_cohen.network.controller.BookController;
import com.dor_cohen.app.dao.DaoFileImpl;
import com.dor_cohen.app.dm.Book;
import com.dor_cohen.algorithm.LRUAlgoCacheImpl;
import com.dor_cohen.app.service.BookService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.List;

public class Server {
    private final int port;
    private final BookController controller;
    private final Gson gson = new Gson();

    public Server(int port) {
        var dao = new DaoFileImpl<Book>("books_store.json", new TypeToken<Map<Long,Book>>(){}.getType());
        var cache = new LRUAlgoCacheImpl<Long,Book>(100);
        var svc = new BookService(dao, cache);
        this.controller = new BookController(svc);
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocket ss = new ServerSocket(port);
        System.out.println("📡 Server started on port " + port + " — waiting for clients...");
        while (true) {
            Socket s = ss.accept();
            new Thread(() -> handle(s)).start();
        }
    }

    private void handle(Socket s) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
            String json = in.readLine();
            Type reqType = new TypeToken<Request<Book>>(){}.getType();
            Request<Book> req = gson.fromJson(json, reqType);
            String act = req.action.toLowerCase();
            String resp = "";
            switch (act) {
                case "add":
                    resp = gson.toJson(controller.add(req.body));
                    break;
                case "get":
                    resp = gson.toJson(controller.get(req.body.getId()));
                    break;
                case "delete":
                    resp = gson.toJson(controller.delete(req.body.getId()));
                    break;
                case "getall":       // <— new!
                    resp = gson.toJson(controller.getAll());
                    break;
                default:
                    Response<String> err = new Response<>();
                    err.success = false;
                    err.message = "Unknown action: " + act;
                    resp = gson.toJson(err);
            }
            out.println(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        new Server(34567).start();
    }
}
