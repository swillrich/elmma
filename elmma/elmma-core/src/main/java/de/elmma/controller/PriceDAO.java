package de.elmma.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.DateType;

import de.elmma.dbio.SessionProvider.HibernateSessionProvider;
import de.elmma.model.Price;

public class PriceDAO {
	/**
	 * liefere alle Kurswerte von Datum zu Datum
	 * @param from
	 * @param to
	 * @return
	 */
	public static List<Price> getPrices(String from, String to) {
		List<Price> prices = (List<Price>) new HibernateSessionProvider() {

			@Override
			public Object work(Session session) {
				Criteria criteria = session.createCriteria(Price.class);
				try {
					if (from != null) {
						criteria.add(Restrictions.sqlRestriction("DATE({alias}.datetime) >= ?", extractDate(from),
								DateType.INSTANCE));
					}
					if (to != null) {
						criteria.add(Restrictions.sqlRestriction("DATE({alias}.datetime) <= ?", extractDate(to),
								DateType.INSTANCE));
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return criteria.list();
			}

			private Date extractDate(String strDate) throws ParseException {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				return format.parse(strDate);
			}
		}.getResult();

		for (int i = 1; i < prices.size(); i++) {
			prices.get(i).consumePrevious(prices.get(0));
		}
		return prices;
	}
}
