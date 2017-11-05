package de.elmma.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.elmma.model.KnockOutOption;
import de.elmma.model.Price;

@RestController
@RequestMapping("/ko")
public class KockOutOptionController {
	@RequestMapping(method = RequestMethod.GET)
	public List<KnockOutOption> ko(@RequestParam(value = "from", required = false) String from,
			@RequestParam(value = "to", required = false) String to) throws ParseException {
		double initialPrice = 2;
		String name = "WKN123456";
		double multiplier = 1d / 100;
		List<KnockOutOption> kos = generateKOsTimeSeries(from, to, initialPrice, name, multiplier);
		return kos;
	}

	@RequestMapping(value = "/csv", method = RequestMethod.GET, produces = { "text/plain" })
	public String csvPrices(@RequestParam(value = "from", required = false) String from,
			@RequestParam(value = "to", required = false) String to) throws ParseException {
		double initialPrice = 2;
		String name = "WKN123456";
		double multiplier = 1d / 100;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		StringBuilder b = new StringBuilder();
		b.append("Datetime,Underlying, KO\n");
		String csvContent = generateKOsTimeSeries(from, to, initialPrice, name, multiplier).stream()
				.map(ko -> format.format(ko.getDatetime()) + "," + ko.getTrackedUnderlying().getPrice() / 1000d + ","
						+ ko.getPrice() + "\n")
				.collect(Collectors.joining());
		b.append(csvContent);
		return b.toString();
	}

	private List<KnockOutOption> generateKOsTimeSeries(String from, String to, double initialPrice, String name,
			double multiplier) {
		List<Price> prices = PriceDAO.getPrices(from, to);
		List<KnockOutOption> kos = new ArrayList<KnockOutOption>();
		KnockOutOption option = new KnockOutOption(name, prices.get(0), prices.get(0), initialPrice, multiplier);
		kos.add(option);
		for (int i = 1; i < prices.size(); i++) {
			Price price = prices.get(i);
			price.consumePrevious(prices.get(i - 1));
			kos.add(new KnockOutOption((KnockOutOption) kos.get(kos.size() - 1), price));
		}
		return kos;
	}
}