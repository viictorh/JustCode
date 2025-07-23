package br.com.victor.justcode.utilities;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.net.ssl.*;

public class Simplepost {
  public static void main(String[] args) throws Exception {
    // Handle --help and --example flags immediately
    for (String arg : args) {
      if ("--help".equals(arg)) {
        Config.printUsage();
        return;
      }

      if ("--example".equals(arg)) {
        Config.printExample();
        return;
      }
    }

    // Parse arguments
    Config cfg = Config.parseArgs(args);

    // Create SSLContext
    SSLContext sslContext = cfg.useKeyStore
        ? createCustomSSLContext(cfg.keystorePath, cfg.keystorePassword)
        : SSLContext.getDefault();

    // Setup HTTP client
    HttpClient.Builder clientBuilder = HttpClient.newBuilder().sslContext(sslContext);

    if (cfg.useProxy) {
      clientBuilder.proxy(ProxySelector.of(new InetSocketAddress(cfg.proxyHost, cfg.proxyPort)));
    }

    HttpClient client = clientBuilder.build();

    // Build request
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
        .uri(URI.create(cfg.url))
        .header("Content-Type", "application/json");

    if (cfg.bearerToken != null && !cfg.bearerToken.isEmpty()) {
      requestBuilder.header("Authorization", "Bearer " + cfg.bearerToken);
    }

    if (cfg.useProxy && cfg.proxyUser != null && !cfg.proxyUser.isEmpty()) {
      String encoded = Base64.getEncoder().encodeToString((cfg.proxyUser + ":" + cfg.proxyPass).getBytes());
      requestBuilder.header("Proxy-Authorization", "Basic " + encoded);
    }

    if ("POST".equalsIgnoreCase(cfg.method)) {
      String body = cfg.bodyFile != null
          ? Files.readString(Paths.get(cfg.bodyFile))
          : cfg.body;

      requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
    } else {
      requestBuilder.GET();
    }

    HttpRequest request = requestBuilder.build();

    // Execute
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    System.out.println("Status: " + response.statusCode());
    System.out.println("Body: " + response.body());
  }

  private static SSLContext createCustomSSLContext(String keystorePath, String keystorePassword) throws Exception {
    KeyStore trustStore = KeyStore.getInstance("JKS");
    try (FileInputStream fis = new FileInputStream(keystorePath)) {
      trustStore.load(fis, keystorePassword.toCharArray());
    }

    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(trustStore);

    // Optionally wrap to print certificates as in your original code
    TrustManager[] tms = tmf.getTrustManagers();
    TrustManager[] wrappedTms = new TrustManager[tms.length];
    for (int i = 0; i < tms.length; i++) {
      final TrustManager tm = tms[i];
      if (tm instanceof X509TrustManager) {
        wrappedTms[i] = new X509TrustManager() {
          final X509TrustManager next = (X509TrustManager) tm;

          @Override
          public void checkClientTrusted(X509Certificate[] chain, String authType) {
            System.out.println("-- Server Presented Certificate Chain ----");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            try {
              for (int j = 0; j < chain.length; j++) {
                X509Certificate cert = chain[j];
                System.out.println("Certificate #" + (j + 1));
                System.out.println("  Subject: " + cert.getSubjectX500Principal());
                System.out.println("  Issuer:  " + cert.getIssuerX500Principal());
                System.out.println("  Serial Number: " + cert.getSerialNumber());
                System.out.println("  Valid from: " + sdf.format(cert.getNotBefore()));
                System.out.println("  Valid until: " + sdf.format(cert.getNotAfter()));
                System.out.println("  Signature Algorithm: " + cert.getSigAlgName());
                System.out.println();
              }

              next.checkServerTrusted(chain, authType);
            } catch (Exception ex) {
              System.out.println("Certificate validation failed: " + ex.getMessage());
            }
          }

          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return next.getAcceptedIssuers();
          }

          @Override
          public void checkServerTrusted(X509Certificate[] chain, String authType) {
            // Not used
          }
        };
      } else {
        wrappedTms[i] = tm;
      }
    }

    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, wrappedTms, null);
    return sslContext;
  }

  // Configuration holder and parser
  static class Config {
    boolean useProxy = false;
    String proxyHost = "localhost";
    int proxyPort = 8080;
    String proxyUser = null;
    String proxyPass = null;

    boolean useKeyStore = false;
    String keystorePath = null;
    String keystorePassword = null;

    String url = "";
    String method = "GET";
    String bearerToken = null;
    String body = "";
    String bodyFile = null;

    static void printUsage() {
      System.out.println("""
          Usage: java Simplepost
              --url <url> [--method GET|POST] [--body <jsonString>] [--bodyFile <file.json>]
              [--token <bearerToken>]
              [--proxy] [--proxyHost <host>] [--proxyPort <port>] [--proxyUser <user>] [--proxyPass <pass>]
              [--keystore] [--keystorePath <path>] [--keystorePassword <password>]
              [--help] [--example]

          Use --example to print a filled sample command with all parameters.
          """);
    }

    static void printExample() {
      String content = """
          java Simplepost \\
              --url https://my.api.com/endpoint \\
              --method POST \\
              --body "{\\"foo\\":\\"bar\\"}" \\
              --token 1234abcDEFg \\
              --proxy \\
              --proxyHost myproxy.corp.com \\
              --proxyPort 3128 \\
              --proxyUser myuser \\
              --proxyPass mypass \\
              --keystore \\
              --keystorePath /path/to/keystore.jks \\
              --keystorePassword changeme
          """;
      System.out.println(content + "\nOr use --bodyFile mybody.json to load JSON from a file.");
      System.out.println("\n\nOne line example: \n" + content.replaceAll("\\\\n", " ").replaceAll("\\s+", " ").trim());
    }

    static Config parseArgs(String[] args) {
      Config cfg = new Config();
      for (int i = 0; i < args.length; i++) {
        switch (args[i]) {
          case "--url" -> cfg.url = args[++i];
          case "--method" -> cfg.method = args[++i].toUpperCase();
          case "--body" -> cfg.body = args[++i];
          case "--bodyFile" -> cfg.bodyFile = args[++i];
          case "--proxy" -> cfg.useProxy = true;
          case "--proxyHost" -> cfg.proxyHost = args[++i];
          case "--proxyPort" -> cfg.proxyPort = Integer.parseInt(args[++i]);
          case "--proxyUser" -> cfg.proxyUser = args[++i];
          case "--proxyPass" -> cfg.proxyPass = args[++i];
          case "--keystore" -> cfg.useKeyStore = true;
          case "--keystorePath" -> cfg.keystorePath = args[++i];
          case "--keystorePassword" -> cfg.keystorePassword = args[++i];
          case "--help", "--example" -> {
            // Already handled in main
          }
          default -> {
            System.err.println("Unknown argument: " + args[i]);
            printUsage();
            System.exit(1);
          }
        }
      }

      if (cfg.url == null || cfg.url.isEmpty()) {
        System.err.println("URL is required.");
        printUsage();
        System.exit(1);
      }

      if ("POST".equalsIgnoreCase(cfg.method) && cfg.body.isEmpty() && cfg.bodyFile == null) {
        System.err.println("POST requests require --body or --bodyFile.");
        printUsage();
        System.exit(1);
      }

      if (cfg.useKeyStore) {
        if (cfg.keystorePath == null || cfg.keystorePassword == null) {
          System.err.println("If --keystore is set, both --keystorePath and --keystorePassword are required.");
          printUsage();
          System.exit(1);
        }
      }

      return cfg;
    }
  }
}
