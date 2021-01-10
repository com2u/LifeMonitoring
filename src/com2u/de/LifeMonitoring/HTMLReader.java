package com2u.de.LifeMonitoring;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.parser.JSONParser;

import twitter4j.JSONArray;
import twitter4j.JSONObject;

class stockData {
	public String name;
	public Double value;
	public int count;
}

public class HTMLReader {

	public static java.util.HashMap<String, stockData> stockValue = new java.util.HashMap<String, stockData>();

	public static void main(String[] args) throws MalformedURLException, IOException {
		java.util.Vector<MQTTMessage> messages = new java.util.Vector<MQTTMessage>();
		for (int i = 1; i < 10 ; i++) {
			ParseHTMLPages(messages);
		}

		// System.out.println(messages);
		String page;
		StringBuilder html;
		String s;
		// page =
		// "https://financialmodelingprep.com/api/v3/stock/real-time-price";

	}

	public static void ParseHTMLPages(java.util.Vector<MQTTMessage> messages)
			throws MalformedURLException, IOException {
		String page;
		StringBuilder html;
		String s;
		page = "https://www.emsc-csem.org/Earthquake/";
		html = readURL(page);
		s = new String(html);
		messages.addAll(extractEartquake(s));

		page = "https://odlinfo.bfs.de/DE/aktuelles/messstellenliste.html";
		html = readURL(page);
		s = new String(html);
		messages.addAll(extractRadiation(s));

		page = "https://financialmodelingprep.com/api/v3/company/stock/list";
		html = readURL(page);
		// System.out.println(html);
		s = new String(html);
		messages.addAll(extractStock(s));
	}

	private static java.util.Vector<MQTTMessage> extractStock(String s) {
		System.out.println("extractStock");
		//System.out.println(s.substring(0,3000));
		
		
		java.util.Vector<MQTTMessage> messages = new java.util.Vector<MQTTMessage>();
		
		try {
			JSONObject obj = new JSONObject(s);
			JSONArray stockArr = obj.getJSONArray("symbolsList");

			for (int i = 0; i < stockArr.length(); i++) {
				String name = stockArr.getJSONObject(i).getString("name");
				String symbol = stockArr.getJSONObject(i).getString("symbol");
				Double value = new Double(stockArr.getJSONObject(i).getString("price"));
				//System.out.println(name + " : " + value);
				if (stockValue.containsKey(symbol)) {
					stockData oldStock = stockValue.get(symbol);
					oldStock.count++;
					//System.out.println(name + " found again " + oldStock.count);
					if ((value > oldStock.value * 1.2) || (value < oldStock.value * 0.7)) {
						System.out.println(name + " is Changing from " + oldStock.value + " to " + value);
						java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
						messages.add(new MQTTMessage(sqlDate, "Stock/" + name, value + ""));
					}
					oldStock.value = value;
					stockValue.put(symbol, oldStock);
				} else {
					stockData data = new stockData();
					data.name = name;
					data.count = 0;
					data.value = value;
					stockValue.put(symbol, data);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return messages;
	}

	private static java.util.Vector<MQTTMessage> extractEartquake(String s) {
		System.out.println("extractEartquake");
		java.util.Vector<MQTTMessage> messages = new java.util.Vector<MQTTMessage>();
		String[] a = s.split("<tr id");
		double magnitute = 0;
		String location = "";
		for (int i = 2; i < a.length - 30; i++) {
			magnitute = new Double(a[i].split("<td class=\"tabev2\">")[3].split("</td><td id")[0]);
			if (magnitute > 6) {
				location = a[i].split("<td class=\"tabev2\">")[3].split("#160")[1];
				location = location.substring(1, location.indexOf("</td>"));
				System.out.println(magnitute + "  " + location);
				java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
				messages.add(new MQTTMessage(sqlDate, "Eartquake/" + location.trim(), magnitute + ""));
			}
		}
		return messages;
	}

	private static java.util.Vector<MQTTMessage> extractRadiation(String s) {
		System.out.println("extractRadiation");
		java.util.Vector<MQTTMessage> messages = new java.util.Vector<MQTTMessage>();
		String[] a = s.split("</div></div><div class=\"row\" data-mst=");
		boolean alert = false;
		//System.out.println("Entries:" + a.length);
		for (int i = 1; i < a.length; i++) {
			// System.out.println("Entries:"+a.length);
			if (a[i].length() > 9) {
				// System.out.println(a[i]);
				Double sie = 0.0;
				String sievert = a[i].substring(a[i].indexOf(">") + 1, a[i].indexOf("&micro;Sv/h"));
				sievert = sievert.substring(sievert.lastIndexOf(">") + 1, sievert.length());
				if (sievert.length() < 20) {
					sievert = sievert.replace(",", ".");

					try {
						sie = new Double(sievert);
					} catch (Exception e) {
						sie = 0.0;
					}
					if (sie > 0.23) {
						alert = true;
					} else {
						alert = false;
					}
				}
				String place = "";
				if (a[i].indexOf("><div class=") > 0) {
					place = a[i].substring(1, a[i].indexOf("><div class=") - 1);
					// System.out.println(place);
					// place = place.substring(26, place.indexOf("div
					// class=\"medium-5 small-8 columns")-3);
					place = place.replaceAll("Ã¶", "ö");
					place = place.replaceAll("Ã¼", "ü");
					place = place.replaceAll("ÃŸ", "ß");
					place = place.replaceAll("Ã–", "ä");
					place = place.replaceAll("Ãœ", "@");
					place = place.replaceAll("Ã¤", "ä");
					if (alert) {
						System.out.print(place);
						for (int t = 5 - (place.length() / 8); t > 0; t--) {
							System.out.print("\t");
						}
					}
				}
				if (alert) {
					System.out.println(sie + " µS ");
					java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
					messages.add(new MQTTMessage(sqlDate, "Radiation/" + place, sie + ""));
				}
			}
		}
		return messages;
	}

	private static StringBuilder readURL(String page) throws MalformedURLException, IOException {
		BufferedReader br = null;
		StringBuilder sb = null;
		try {

			URL url = new URL(page);
			br = new BufferedReader(new InputStreamReader(url.openStream()));

			String line;

			sb = new StringBuilder();

			while ((line = br.readLine()) != null) {

				sb.append(line);
				sb.append(System.lineSeparator());
			}

			// System.out.println(sb);

		} finally {

			if (br != null) {
				br.close();
			}
		}
		return sb;

	}

}
