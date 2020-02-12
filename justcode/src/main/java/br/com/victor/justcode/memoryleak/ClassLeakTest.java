package br.com.victor.justcode.memoryleak;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassLeakTest {
	public static void main(String[] args) throws InterruptedException {
		List<Object> l2 = new ArrayList<>();
		for (int i = 0; i < 50_000; i++) {
			List<Object> l = new ArrayList<>();
			for (int j = 0; j < 100_000; j++) {
				l.add(ClassLeak.load());
			}
			Thread.sleep(3000);
			l2.add(l);
		}
			System.out.println("finish1");
		Thread.sleep(30000);
		System.out.println("finish");
	}
}

class ClassLeak {

	public static BotLoad load() {
		return new BotLoad() {
			{
				setMetodoA("");
				setMetodoB("");
				setMetodoC("");
				HashMap<String, String> hashMap = new HashMap<String, String>() {
					{
						put("BotBSF.STATES.CNPJ", "#CNPJ");
						put("BotBSF.STATES.CPF", "#CPF");
						put("BotBSF.STATES.FUNERAL", "#FUNERAL");
						put("BotBSF.STATES.ATENDENTE", "#ATENDENTE");
						put("MAX_INPUT_ERROR", "/MAX_INPUT_ERROR");
						put("MAX_NO_INPUT", "/MAX_NO_INPUT");
					}
				};
				setMetodoMap(hashMap);
			}
		};
	}
}