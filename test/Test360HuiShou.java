import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.doumiao.joke.lang.HttpClientHelper;

public class Test360HuiShou {

	private Log log = LogFactory.getLog(getClass());
	static Map<String, Float> ratio = new HashMap<String, Float>();
	static {
		ratio.put("221", 1f);
		ratio.put("222", 0.97f);
		ratio.put("223", 0.85f);
		ratio.put("224", 0.72f);
		ratio.put("225", 0.05f);

		ratio.put("241", 1f);
		ratio.put("242", 1f);
		ratio.put("245", 1.13f);

		ratio.put("101", 1f);
		ratio.put("102", 1.25f);
		ratio.put("103", 0.55f);
		ratio.put("104", 1.30f);

		ratio.put("991", 1f);
		ratio.put("992", 1f);
		ratio.put("993", 0.05f);

		ratio.put("151", 1f);
		ratio.put("152", 1f);
		ratio.put("153", 1f);
		ratio.put("154", 1f);
		ratio.put("155", 1f);
		ratio.put("156", 1f);

		ratio.put("111", 1f);
		ratio.put("112", 0.89f);
		ratio.put("113", 0.76f);
		ratio.put("114", 0.65f);

		ratio.put("211", 1f);
		ratio.put("161", 1f);
		ratio.put("171", 1f);
		ratio.put("181", 1f);
		ratio.put("121", 1f);
		ratio.put("201", 1f);
		ratio.put("231", 1f);
		ratio.put("131", 1f);
		ratio.put("191", 1f);
		ratio.put("141", 1f);
		
		ratio.put("212", 0.75f);
		ratio.put("162", 0.85f);
		ratio.put("172", 0.5f);
		ratio.put("182", 0f);
		ratio.put("122", 0.1f);
		ratio.put("202", 0.5f);
		ratio.put("233", 0f);
		ratio.put("232", 0f);
		ratio.put("192", 0.5f);
		ratio.put("142", 0f);
		ratio.put("143", 0f);
		Map<Integer, Float> modelPrice = new HashMap<Integer, Float>();
	}

	@Test
	public void fetch() throws Exception {
		String model_id = "20782";
		String url = "http://bang.360.cn/huishou/pinggu/?model_id=" + model_id;
		HttpClient client = HttpClientHelper.getClient();
		HttpGet get = new HttpGet(url);
		Document listDoc = null;
		try {
			HttpResponse response = client.execute(get);
			listDoc = Jsoup.parse(EntityUtils.toString(response.getEntity(),
					"utf-8"));
		} catch (Exception e) {
			log.error(e, e);
		} finally {
			get.releaseConnection();
		}
		Elements es = listDoc.select("div.phone-info-choice");
		List<Map<String, Map<String, String>>> l = new ArrayList<Map<String, Map<String, String>>>(
				es.size());
		for (int i = 0; i < es.size(); i++) {
			Element e = es.get(i);
			Element _e = e.select("h2").get(0);
			String title = _e.text();
			Elements _es = e.select("a.check-item");
			Map<String, Map<String, String>> m = new HashMap<String, Map<String, String>>(
					1);
			l.add(m);
			Map<String, String> _m = new HashMap<String, String>(_es.size());
			m.put(title, _m);
			for (int j = 0; j < _es.size(); j++) {
				Element __e = _es.get(j);
				String key = "选择手机基本情况".equals(title) ? __e
						.attr("data-default") + "_" + __e.attr("data-select")
						: __e.attr("data-select");
				String value = __e.text();
				_m.put(key, value);
			}
		}
		log.info("params fetch over.");
		log.info("compine link start");
		List<String> links = new ArrayList<String>();
		links.add("");
		for (Map<String, Map<String, String>> map : l) {
			String title = map.keySet().iterator().next();
			Map<String, String> values = map.values().iterator().next();
			if ("选择手机基本情况".equals(title)) {
				List<String> nLinks = new ArrayList<String>(links.size()
						* values.size());
				Object[] v_array = values.keySet().toArray();
				for (String ll : links) {
					for (int i = 0; i < v_array.length; i++) {
						Set<String> repeat = new HashSet<String>();
						for (int j = 0; j < v_array.length; j++) {
							String[] v = ((String)v_array[j]).split("_");
							if (i == j) {
								repeat.add(v[1]);
							} else {
								repeat.add(v[0]);
							}
						}
						if(StringUtils.isNotBlank(ll)){
							nLinks.add(ll+","+StringUtils.join(repeat,","));
						}else{
							nLinks.add(StringUtils.join(repeat,","));
						}
					}
					Set<String> repeat = new HashSet<String>();
					for (int i = 0; i < v_array.length; i++) {
						String[] v = ((String)v_array[i]).split("_");
						repeat.add(v[0]);
					}
					if(StringUtils.isNotBlank(ll)){
						nLinks.add(ll+","+StringUtils.join(repeat,","));
					}else{
						nLinks.add(StringUtils.join(repeat,","));
					}
				}
				links = nLinks;
			} else {
				List<String> nLinks = new ArrayList<String>(links.size()
						* values.size());
				if (values.size() > 0) {
					for (String ll : links) {
						for (String k : values.keySet()) {
							nLinks.add(ll + "," + k);
						}
					}
					links = nLinks;
				}
			}

		}
		Map<String, Float> result = new HashMap<String, Float>(links.size());
		File file = new File(model_id);

		if (file.exists()) {
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			String line;
			while ((line = raf.readLine()) != null) {
				String[] data = line.split(":");
				result.put(data[0], Float.parseFloat(data[1]));
			}
			raf.close();
		} else {
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			try {
				for (String ll : links) {
					try {
						String link = "http://bang.360.cn/huishou/pinggu_shop/?sub_options=city=%E5%8C%97%E4%BA%AC&model_id="
								+ model_id + "&sub_options=" + ll;
						get = new HttpGet(link);

						HttpResponse response = client.execute(get);
						listDoc = Jsoup.parse(EntityUtils.toString(
								response.getEntity(), "utf-8"));
						String text = listDoc.select("h3.p-b-money").get(0)
								.childNode(0).toString();
						Pattern p = Pattern.compile("￥(\\d*).*");
						Matcher m = p.matcher(text);
						if (m.find()) {
							Float price = Float.parseFloat(m.group(1));
							result.put(ll, price);
							raf.writeBytes(ll + ":" + price + "\r\n");
						}
					} catch (Exception e) {
						log.error("time out");
					}
				}
			} catch (Exception e) {
				log.error(e, e);
			} finally {
				raf.close();
				get.releaseConnection();
			}
		}

		String baseKey = "161,211,171,181,121,201,231,191,131,141,221";
		Pattern groupKey = Pattern
				.compile(".*,221");
		Matcher gm;
		if (StringUtils.isNotBlank(baseKey) && result.containsKey(baseKey)) {
			Float basePrice = result.get(baseKey);
			for (String key : result.keySet()) {
				// if (key.equals(baseKey))
				// continue;
				gm = groupKey.matcher(key);
				if (gm.find()) {
					Float price = result.get(key);
					log.info(key + ":" + price + ":" + price / basePrice);
				}
			}
		}
		for (String key : result.keySet()) {
			Float price = result.get(key);
			Float ori_price = price;
			String[] ratio_array = key.split(",");
			if (key.contains("154")) {
				price = price - 20;
			}

			for (String r : ratio_array) {
				price = price / ratio.get(r);
			}
			log.info(key + ":" + ori_price + ":" + price);
		}
	}
}
