package br.com.victor.justcode.request;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class FloodRequest {

	private static Map<String, Object> parameters;
	private static ExecutorService reqExecutor = Executors.newCachedThreadPool();
	private static ExecutorService usersExecutors = Executors.newCachedThreadPool();
	private static final String URL = "http://172.30.51.10:8983/solr/core1/select?";
	private static AtomicInteger threadCount = new AtomicInteger(0);
	private static AtomicInteger requestCount = new AtomicInteger(0);

	static {
		Unirest.setTimeouts(60000, 60000);
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger("org.apache.http");
		root.setLevel(ch.qos.logback.classic.Level.INFO);
		parameters = new HashMap<>();
		parameters.put("indent", "on");
		parameters.put("rows", "500");
		parameters.put("q",
				"*11728182780 DtInitial%3A%5B2020-08-27T00%3A05%3A00.000Z%20TO%202020-08-27T23%3A59%3A59.999Z%5D");
	}

	public static void main(String[] args) throws UnirestException, InterruptedException, ExecutionException {
		int quantity = 20;
		if (args.length > 0) {
			quantity = Integer.parseInt(args[0]);
		}
		for (int i = 0; i < quantity; i++) {
			int localThreadCount = threadCount.incrementAndGet();
			usersExecutors.submit(() -> {
				while (true) {
					Future<String> f = reqExecutor.submit(() -> threadRequest(localThreadCount));
					System.out.println(f.get());
					TimeUnit.SECONDS.sleep(1);
				}
			});
		}
		System.out.println("DONE");

	}

	private static String threadRequest(int localThreadCount) {
		try {
			System.out.println(LocalDateTime.now() + " - Iniciando execução: [" + localThreadCount + "]");
			HashMap<String, Object> a = new HashMap<>();

			a.put("indent", "on");
			a.put("	q.op", "AND");
			a.put("	df", "Anything");
			a.put("			rows", "500");
			a.put("q", "??1" + System.currentTimeMillis()
					+ "%20DtInitial%3A%5B2020-08-27T00%3A05%3A00.000Z%20TO%202020-08-27T23%3A59%3A59.999Z%5D");

			HttpResponse<String> result = Unirest.get(URL).queryString(a).asString();
			int incrementAndGet = requestCount.incrementAndGet();
			return "Total [" + incrementAndGet + "] " + LocalDateTime.now() + " - [" + localThreadCount
					+ "] - RESULT STATUS: [" + result.getStatusText() + "]";
		} catch (Exception e) {
			System.out.println("Erro: [" + e.getMessage() + "]");
			return "ERRO!";
		}
	}

}
