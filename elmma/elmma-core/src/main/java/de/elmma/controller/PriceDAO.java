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
	public static List<Price> getPrices(String from, String to) {
		return (List<Price>) new HibernateSessionProvider() {

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
	}
}
