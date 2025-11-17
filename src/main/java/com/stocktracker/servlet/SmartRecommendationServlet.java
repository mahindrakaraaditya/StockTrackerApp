// // package com.stocktracker.servlet;

// // import jakarta.servlet.*;
// // import jakarta.servlet.http.*;
// // import jakarta.servlet.annotation.*;
// // import org.json.JSONObject;
// // import org.json.JSONArray;
// // import java.io.*;
// // import java.net.*;
// // import java.util.*;

// // import com.stocktracker.model.Stock;

// // @WebServlet("/recommendations")
// // public class StockRecommendationServlet extends HttpServlet {
// //     private static final String API_KEY = "Z1S7WHU6NLKRWSXZ"; // Replace with your own AlphaVantage key

// //     @Override
// //     protected void doGet(HttpServletRequest request, HttpServletResponse response)
// //             throws ServletException, IOException {

// //         // Example suggested stocks (could later be user-personalized)
// //         String[] symbols = {"AAPL", "MSFT", "GOOGL", "TSLA", "AMZN"};

// //         List<Stock> recommendedStocks = new ArrayList<>();

// //         for (String symbol : symbols) {
// //             try {
// //                 String apiUrl = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
// //                         + symbol + "&apikey=" + API_KEY;

// //                 URL url = new URL(apiUrl);
// //                 BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
// //                 StringBuilder jsonResponse = new StringBuilder();
// //                 String line;
// //                 while ((line = reader.readLine()) != null)
// //                     jsonResponse.append(line);
// //                 reader.close();

// //                 JSONObject json = new JSONObject(jsonResponse.toString());
// //                 JSONObject quote = json.getJSONObject("Global Quote");

// //                 String companySymbol = quote.getString("01. symbol");
// //                 double price = Double.parseDouble(quote.getString("05. price"));
// //                 double change = Double.parseDouble(quote.getString("09. change"));
// //                 double changePercent = Double.parseDouble(quote.getString("10. change percent").replace("%", ""));

// //                 recommendedStocks.add(new Stock(0, companySymbol, "N/A", price, change, changePercent));

// //             } catch (Exception e) {
// //                 e.printStackTrace();
// //             }
// //         }

// //         request.setAttribute("recommendedStocks", recommendedStocks);
// //         request.getRequestDispatcher("recommendations.jsp").forward(request, response);
// //     }
// // }

// package com.stocktracker.servlet;

// import jakarta.servlet.*;
// import jakarta.servlet.http.*;
// import jakarta.servlet.annotation.*;
// import org.json.*;
// import java.io.*;
// import java.net.*;
// import java.util.*;

// import com.stocktracker.dao.TransactionDAO;
// import com.stocktracker.dao.WatchlistDAO;
// import com.stocktracker.model.Stock;
// import com.stocktracker.model.User;

// @WebServlet("/smartRecommendations")
// public class SmartRecommendationServlet extends HttpServlet {
//     private static final String API_KEY = "Z1S7WHU6NLKRWSXZ"; // your Alpha Vantage key

//     private List<String> popularStocks = Arrays.asList(
//         "AAPL","MSFT","TSLA","GOOGL","AMZN","META","NVDA","NFLX","DIS","IBM"
//     );

//     @Override
//     protected void doGet(HttpServletRequest request, HttpServletResponse response)
//             throws ServletException, IOException {

//         HttpSession session = request.getSession();
//         User user = (User) session.getAttribute("user");

//         if (user == null) {
//             response.sendRedirect("login.html");
//             return;
//         }

//         List<Stock> personalized = new ArrayList<>();
//         List<Stock> trending = new ArrayList<>();
//         List<Stock> randomSuggestions = new ArrayList<>();

//         // --- 1️⃣ Personalized Suggestions based on watchlist + transactions ---
//         try {
//             WatchlistDAO wdao = new WatchlistDAO();
//             TransactionDAO tdao = new TransactionDAO();

//             Set<String> interestedSymbols = new HashSet<>();
//             interestedSymbols.addAll(wdao.getUserSymbols(user.getId()));
//             interestedSymbols.addAll(tdao.getUserSymbols(user.getId()));

//             for (String symbol : interestedSymbols) {
//                 Stock s = fetchStockData(symbol);
//                 if (s != null) personalized.add(s);
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }

//         // --- 2️⃣ Trending Stocks (using AlphaVantage TOP_GAINERS_LOSERS endpoint) ---
//         try {
//             String apiUrl = "https://www.alphavantage.co/query?function=TOP_GAINERS_LOSERS&apikey=" + API_KEY;
//             URL url = new URL(apiUrl);
//             BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
//             StringBuilder sb = new StringBuilder();
//             String line;
//             while ((line = reader.readLine()) != null) sb.append(line);
//             reader.close();

//             JSONObject json = new JSONObject(sb.toString());
//             JSONArray topGainers = json.getJSONArray("top_gainers");

//             for (int i = 0; i < Math.min(5, topGainers.length()); i++) {
//                 JSONObject g = topGainers.getJSONObject(i);
//                 trending.add(new Stock(0, g.getString("ticker"), "N/A",
//                         Double.parseDouble(g.getString("price")),
//                         Double.parseDouble(g.getString("change_amount")),
//                         Double.parseDouble(g.getString("change_percentage").replace("%", ""))));
//             }
//         } catch (Exception e) {
//             // fallback to static list if API limit hit
//             for (String symbol : popularStocks.subList(0, 5)) {
//                 Stock s = fetchStockData(symbol);
//                 if (s != null) trending.add(s);
//             }
//         }

//         // --- 3️⃣ Random Discovery Suggestions ---
//         Collections.shuffle(popularStocks);
//         for (String symbol : popularStocks.subList(0, 3)) {
//             Stock s = fetchStockData(symbol);
//             if (s != null) randomSuggestions.add(s);
//         }

//         request.setAttribute("personalized", personalized);
//         request.setAttribute("trending", trending);
//         request.setAttribute("randomSuggestions", randomSuggestions);

//         request.getRequestDispatcher("smartRecommendations.jsp").forward(request, response);
//     }

//     // --- Helper method to fetch stock data ---
//     private Stock fetchStockData(String symbol) {
//         try {
//             String apiUrl = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
//                     + symbol + "&apikey=" + API_KEY;

//             URL url = new URL(apiUrl);
//             BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
//             StringBuilder sb = new StringBuilder();
//             String line;
//             while ((line = reader.readLine()) != null)
//                 sb.append(line);
//             reader.close();

//             JSONObject json = new JSONObject(sb.toString());
//             JSONObject quote = json.getJSONObject("Global Quote");

//             double price = Double.parseDouble(quote.getString("05. price"));
//             double change = Double.parseDouble(quote.getString("09. change"));
//             double changePercent = Double.parseDouble(quote.getString("10. change percent").replace("%", ""));

//             return new Stock(0, symbol, "N/A", price, change, changePercent);

//         } catch (Exception e) {
//             return null;
//         }
//     }
// }
package com.stocktracker.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.stocktracker.dao.TransactionDAO;
import com.stocktracker.dao.WatchlistDAO;
import com.stocktracker.model.Stock;
import com.stocktracker.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/smartRecommendations")
public class SmartRecommendationServlet extends HttpServlet {
    private static final String API_KEY = "Z1S7WHU6NLKRWSXZ"; // replace if needed
    private static final List<String> POPULAR_STOCKS = Arrays.asList(
        "AAPL","MSFT","TSLA","GOOGL","AMZN","META","NVDA","NFLX","DIS","IBM"
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        System.out.println("[smartRecommendations] session=" + session);
        if (session == null) {
            System.out.println("[smartRecommendations] session is null -> redirect to login");
            response.sendRedirect("login.html");
            return;
        }

        User user = (User) session.getAttribute("user");
        System.out.println("[smartRecommendations] user=" + user);
        if (user == null) {
            System.out.println("[smartRecommendations] user attribute is null -> redirect to login");
            response.sendRedirect("login.html");
            return;
        }

        List<Stock> personalized = new ArrayList<>();
        List<Stock> trending = new ArrayList<>();
        List<Stock> randomSuggestions = new ArrayList<>();
        boolean usedFallback = false;

        // 1) Personalized: watchlist + transactions (null-safe)
        try {
            WatchlistDAO wdao = new WatchlistDAO();
            TransactionDAO tdao = new TransactionDAO();

            Set<String> interestedSymbols = new HashSet<>();
            Set<String> watchSymbolsRaw = safeToSet(wdao.getUserSymbols(user.getId()));
            Set<String> transSymbolsRaw = safeToSet(tdao.getUserSymbols(user.getId()));

            System.out.println("[smartRecommendations] watchSymbols.size=" + (watchSymbolsRaw==null?0:watchSymbolsRaw.size()));
            System.out.println("[smartRecommendations] transSymbols.size=" + (transSymbolsRaw==null?0:transSymbolsRaw.size()));

            if (watchSymbolsRaw != null) interestedSymbols.addAll(watchSymbolsRaw);
            if (transSymbolsRaw != null) interestedSymbols.addAll(transSymbolsRaw);

            for (String symbol : interestedSymbols) {
                Stock s = fetchStockData(symbol);
                if (s != null) personalized.add(s);
            }
            System.out.println("[smartRecommendations] personalized.size=" + personalized.size());
        } catch (Exception e) {
            System.out.println("[smartRecommendations] error while building personalized list: " + e.getMessage());
            e.printStackTrace();
        }

        // 2) Trending - attempt API, but many public APIs don't provide this function.
        try {
            // NOTE: AlphaVantage doesn't provide "TOP_GAINERS_LOSERS" on free API (likely), so this may throw
            String apiUrl = "https://www.alphavantage.co/query?function=TOP_GAINERS_LOSERS&apikey=" + API_KEY;
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");

            int rc = conn.getResponseCode();
            InputStream is = (rc >= 200 && rc < 300) ? conn.getInputStream() : conn.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            String raw = sb.toString();
            System.out.println("[smartRecommendations] trending raw response: " + (raw.length() > 200 ? raw.substring(0,200) + "..." : raw));

            JSONObject json = new JSONObject(raw);
            if (json.has("top_gainers")) {
                JSONArray topGainers = json.getJSONArray("top_gainers");
                for (int i = 0; i < Math.min(5, topGainers.length()); i++) {
                    JSONObject g = topGainers.getJSONObject(i);
                    try {
                        trending.add(new Stock(0, g.optString("ticker", "N/A"), "N/A",
                                Double.parseDouble(g.optString("price", "0")),
                                Double.parseDouble(g.optString("change_amount", "0")),
                                Double.parseDouble(g.optString("change_percentage", "0").replace("%", ""))));
                    } catch (NumberFormatException nfe) {
                        System.out.println("[smartRecommendations] parse error for trending item: " + g);
                    }
                }
            } else {
                // API didn't return expected structure -> fallback
                System.out.println("[smartRecommendations] trending API did not return top_gainers. Using fallback.");
                usedFallback = true;
            }
        } catch (Exception e) {
            System.out.println("[smartRecommendations] trending API error, falling back to static list: " + e.getMessage());
            usedFallback = true;
        }

        if (usedFallback || trending.isEmpty()) {
            for (String symbol : POPULAR_STOCKS.subList(0, 5)) {
                Stock s = fetchStockData(symbol);
                if (s != null) trending.add(s);
            }
            System.out.println("[smartRecommendations] trending.size after fallback=" + trending.size());
        }

        // 3) Random discovery
        List<String> shuffled = new ArrayList<>(POPULAR_STOCKS);
        Collections.shuffle(shuffled);
        for (String symbol : shuffled.subList(0, 3)) {
            Stock s = fetchStockData(symbol);
            if (s != null) randomSuggestions.add(s);
        }

        System.out.println("[smartRecommendations] final sizes: personalized=" + personalized.size()
                + ", trending=" + trending.size() + ", random=" + randomSuggestions.size());

        // If everything is empty, inject a local fallback so JSP shows samples and set recommendationFallback
        if ((personalized == null || personalized.isEmpty())
                && (trending == null || trending.isEmpty())
                && (randomSuggestions == null || randomSuggestions.isEmpty())) {

            System.out.println("[smartRecommendations] All lists empty -> injecting local fallback sample stocks for debugging.");
            List<Stock> fallback = new ArrayList<>();
            fallback.add(new Stock(0, "AAPL", "Apple Inc.", 170.25, 1.12, 0.66));
            fallback.add(new Stock(0, "MSFT", "Microsoft Corp.", 331.10, -0.50, -0.15));
            fallback.add(new Stock(0, "GOOGL", "Alphabet Inc.", 125.40, 2.05, 1.66));

            personalized = new ArrayList<>(fallback);
            trending = new ArrayList<>(fallback);
            randomSuggestions = new ArrayList<>(fallback);

            usedFallback = true; // mark that fallback was used
        }

        request.setAttribute("personalized", personalized);
        request.setAttribute("trending", trending);
        request.setAttribute("randomSuggestions", randomSuggestions);
        request.setAttribute("recommendationFallback", usedFallback);

        request.getRequestDispatcher("smartRecommendations.jsp").forward(request, response);
    }

    // Helper: null-safe conversion from List to Set
    private Set<String> safeToSet(List<String> input) {
        if (input == null) return new HashSet<>();
        return new HashSet<>(input);
    }

    private Stock fetchStockData(String symbol) {
        try {
            String safeSymbol = URLEncoder.encode(symbol, "UTF-8");
            String apiUrl = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                    + safeSymbol + "&apikey=" + API_KEY;

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");

            int rc = conn.getResponseCode();
            InputStream is = (rc >= 200 && rc < 300) ? conn.getInputStream() : conn.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            String raw = sb.toString();
            System.out.println("[fetchStockData] raw for " + symbol + ": " + (raw.length() > 200 ? raw.substring(0,200) + "..." : raw));

            JSONObject json = new JSONObject(raw);
            JSONObject quote = json.optJSONObject("Global Quote");
            if (quote == null || quote.length() == 0) {
                System.out.println("[fetchStockData] no Global Quote or empty for " + symbol);
                return null;
            }

            String priceStr = quote.optString("05. price", "0");
            String changeStr = quote.optString("09. change", "0");
            String changePercentStr = quote.optString("10. change percent", "0").replace("%", "");

            double price = Double.parseDouble(priceStr);
            double change = Double.parseDouble(changeStr);
            double changePercent = Double.parseDouble(changePercentStr);

            return new Stock(0, symbol, "N/A", price, change, changePercent);

        } catch (Exception e) {
            System.out.println("[fetchStockData] error for " + symbol + " -> " + e.getMessage());
            return null;
        }
    }
}
