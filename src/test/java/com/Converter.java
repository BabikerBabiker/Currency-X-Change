package com;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Converter {
  private static final Scanner scanner = new Scanner(System.in);

  public static void cls() {
    for (int i = 0; i < 50; i++) {
      System.out.println();
    }
  }

  public static void exit() {
    cls();
    System.out.print("Exiting");

    for (int i = 1; i <= 3; ++i) {
      System.out.print(".");

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.exit(0);
  }

  public static void mainMenu() {
    cls();
    int choice;
    do {
      cls();
      System.out.println("[1] Show Supported Currencies");
      System.out.println("[2] Make a Conversion");
      System.out.println("[3] Exit Program");
      System.out.print("Enter your choice: ");
      choice = scanner.nextInt();

      switch (choice) {
        case 1:
          cls();
          supported();
          break;
        case 2:
          cls();
          convert();
          break;
        case 3:
          cls();
          exit();
          break;
        default:
          System.out.println("Invalid choice. Please try again.");
      }
    } while (choice != 3);

    scanner.close();
  }

  public static void supported() {
    OkHttpClient client = new OkHttpClient();

    try {
      String baseUrl = "https://api.apilayer.com/fixer/symbols";
      Request request =
          new Request.Builder()
              .url(baseUrl)
              .addHeader("apikey", "ynjG9UQvAVSbY42qE2VOxFFmTPE2twk1")
              .build();

      Response response = client.newCall(request).execute();

      if (response.isSuccessful()) {
        String responseBody = response.body().string();
        JSONObject jsonObject = new JSONObject(responseBody);

        if (jsonObject.has("symbols")) {
          JSONObject symbolsObject = jsonObject.getJSONObject("symbols");
          System.out.println("Supported Currencies");
          System.out.println("--------------------");

          for (String currency : symbolsObject.keySet()) {
            String currencyName = symbolsObject.getString(currency);
            System.out.println(currency + " - " + currencyName);
          }
        } else {
          System.out.println("Symbols data not found in API response");
        }
      } else {
        System.out.println(
            "Request failed: " + response.code() + " " + response.message());
      }

    } catch (Exception e) {
      System.err.println("An error occurred: " + e.getMessage());
    }

    System.out.println("\nDo you want to return to the main menu? (Y/N)");
    String answer = scanner.next().toUpperCase();

    if (answer.equals("Y")) {
      cls();
      return;
    } else {
      exit();
    }
  }

  public static void convert() {
    OkHttpClient client = new OkHttpClient();
    boolean validInput = false;

    while (!validInput) {
      try {
        cls();

        System.out.print("Enter base currency (e.g., USD): ");
        String baseCurrency = scanner.next().toUpperCase();

        System.out.print(
            "Enter currency symbols to convert to (comma-separated, e.g., EUR,GBP): ");
        scanner.nextLine();
        String input = scanner.nextLine().toUpperCase();

        String[] symbolArray = input.split(",");
        List<String> unsupportedCurrencies = new ArrayList<>();

        for (String targetCurrency : symbolArray) {
          if (!isCurrencySupported(targetCurrency.trim())) {
            unsupportedCurrencies.add(targetCurrency.trim());
          }
        }

        if (!unsupportedCurrencies.isEmpty()) {
          System.out.println(
              "The following currencies are not supported by the API: "
              + unsupportedCurrencies);
          System.out.println("Please try again with valid currency symbols.");
          System.out.println("\nPress Enter to continue...");
          scanner.nextLine();
        } else {
          validInput = true;
          BigDecimal amount = getAmountToConvert();

          performConversion(baseCurrency, symbolArray, amount);
        }
      } catch (Exception e) {
        System.err.println("An error occurred: " + e.getMessage());
      }
    }
  }

  private static void performConversion(
      String baseCurrency, String[] symbols, BigDecimal amount) {
    OkHttpClient client = new OkHttpClient();

    try {
      cls();

      String symbolString = String.join(",", symbols);
      String baseUrl = "https://api.apilayer.com/fixer/latest";
      String url =
          baseUrl + "?symbols=" + symbolString + "&base=" + baseCurrency;

      Request request =
          new Request.Builder()
              .url(url)
              .addHeader("apikey", "ynjG9UQvAVSbY42qE2VOxFFmTPE2twk1")
              .build();

      Response response = client.newCall(request).execute();

      if (response.isSuccessful()) {
        String responseBody = response.body().string();
        JSONObject jsonObject = new JSONObject(responseBody);

        if (jsonObject.has("rates")) {
          JSONObject ratesObject = jsonObject.getJSONObject("rates");

          for (String targetCurrency : symbols) {
            BigDecimal rate = ratesObject.getBigDecimal(targetCurrency);
            BigDecimal convertedAmount = amount.multiply(rate);
            System.out.println(amount + " " + baseCurrency + " = "
                + convertedAmount + " " + targetCurrency);
          }

          System.out.println("\nDo you want to return to the main menu? (Y/N)");
          String answer = scanner.next().toUpperCase();

          if (answer.equals("Y")) {
            return;
          } else {
            exit();
          }
        } else {
          System.out.println("Rates data not found in API response");
        }
      } else {
        System.out.println(
            "Request failed: " + response.code() + " " + response.message());
      }
    } catch (Exception e) {
      System.err.println("An error occurred: " + e.getMessage());
    }
  }

  private static BigDecimal getAmountToConvert() {
    System.out.print("Enter amount to convert: ");
    return scanner.nextBigDecimal();
  }

  private static boolean isCurrencySupported(String currency) {
    OkHttpClient client = new OkHttpClient();

    try {
      String baseUrl = "https://api.apilayer.com/fixer/symbols";
      Request request =
          new Request.Builder()
              .url(baseUrl)
              .addHeader("apikey", "ynjG9UQvAVSbY42qE2VOxFFmTPE2twk1")
              .build();

      Response response = client.newCall(request).execute();

      if (response.isSuccessful()) {
        String responseBody = response.body().string();
        JSONObject jsonObject = new JSONObject(responseBody);

        if (jsonObject.has("symbols")) {
          JSONObject symbolsObject = jsonObject.getJSONObject("symbols");
          return symbolsObject.has(currency);
        }
      } else {
        System.out.println(
            "Request failed: " + response.code() + " " + response.message());
      }
    } catch (Exception e) {
      System.err.println("An error occurred while checking currency support: "
          + e.getMessage());
    }

    return false;
  }

  public static void main(String[] args) {
    mainMenu();
  }
}