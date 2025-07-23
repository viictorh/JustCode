package br.com.victor.justcode.memoryleak;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GoodClass {

	public static void main(String[] args) throws InterruptedException {
		List<Object> l2 = new ArrayList<>();
		for (int i = 0; i < 50_000; i++) {
			List<Object> l = new ArrayList<>();
			for (int j = 0; j < 100_000; j++) {
				l.add(NotLeaked.load());
			}
			Thread.sleep(3000);
			l2.add(l);
		}
			System.out.println("finish1");
		Thread.sleep(30000);
		System.out.println("finish");
	}

}

class NotLeaked {

	private final static HashMap<String, String> hashMap;

	static {
		hashMap = new HashMap<String, String>();
		hashMap.put("BotBSF.STATES.CNPJ", "#CNPJ");
		hashMap.put("BotBSF.STATES.CPF", "#CPF");
		hashMap.put("BotBSF.STATES.FUNERAL", "#FUNERAL");
		hashMap.put("BotBSF.STATES.ATENDENTE", "#ATENDENTE");
		hashMap.put("MAX_INPUT_ERROR", "/MAX_INPUT_ERROR");
		hashMap.put("MAX_NO_INPUT", "/MAX_NO_INPUT");
	}

	public static BotLoad load() {
		BotLoad botLoad = new BotLoad();
		botLoad.setMetodoA("");
		botLoad.setMetodoB("");
		botLoad.setMetodoC("");
		botLoad.setMetodoMap(hashMap);
		return botLoad;
	}
}
