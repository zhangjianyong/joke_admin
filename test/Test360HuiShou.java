import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
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

import sun.swing.StringUIClientPropertyKey;

import com.doumiao.joke.lang.HttpClientHelper;

public class Test360HuiShou {

	private Log log = LogFactory.getLog(getClass());

	@Test
	public void fetch() throws Exception {
		String url = "http://bang.360.cn/huishou/pinggu/?model_id=12777";
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
				String key = __e.attr("data-select");
				String value = __e.text();
				_m.put(key, value);
			}
		}
		log.info("params fetch over.");
		log.info("compine link start");
		List<String> links = new ArrayList<String>();
		for (Map<String, Map<String, String>> map : l) {
			String title = map.keySet().iterator().next();
			if ("选择手机基本情况".equals(title)) {
				Map<String, String> values = map.values().iterator().next();
				links.add(StringUtils.join(values.keySet().toArray(),","));
			} else {
				Iterator<Map<String, String>> it = map.values().iterator();
				while (it.hasNext()) {
					Map<String,String> m = it.next();
					Map<String, String> it = m.values().iterator();
					while (it.hasNext()) {
						Map<String,String> m = it.next();
						
					}
				}
			}
		}
	}
	// if ("选择手机基本情况".equals(title)) {
	// Elements _es = e.select("a.check-item");
	// for (int j = 0; j < _es.size(); j++) {
	// Element __e = _es.get(j);
	// log.info(__e.text());
	// }
	// } else if ("机身颜色".equals(title)) {
	// Elements _es = e.select("a.check-item");
	// for (int j = 0; j < _es.size(); j++) {
	// Element __e = _es.get(j);
	// log.info(__e.text());
	// }
	// } else if ("购买渠道".equals(title)) {
	// Elements _es = e.select("a.check-item");
	// for (int j = 0; j < _es.size(); j++) {
	// Element __e = _es.get(j);
	// log.info(__e.text());
	// }
	// } else if ("保修情况".equals(title)) {
	// Elements _es = e.select("a.check-item");
	// for (int j = 0; j < _es.size(); j++) {
	// Element __e = _es.get(j);
	// log.info(__e.text());
	// }
	// } else if ("储存容量".equals(title)) {
	// Elements _es = e.select("a.check-item");
	// for (int j = 0; j < _es.size(); j++) {
	// Element __e = _es.get(j);
	// log.info(__e.text());
	// }
	// } else if ("iCloud是否有锁".equals(title)) {
	// Elements _es = e.select("a.check-item");
	// for (int j = 0; j < _es.size(); j++) {
	// Element __e = _es.get(j);
	// log.info(__e.text());
	// }
	// } else if ("成色".equals(title)) {
	// Elements _es = e.select("a.check-item");
	// for (int j = 0; j < _es.size(); j++) {
	// Element __e = _es.get(j);
	// log.info(__e.text());
	// }
	// } else if ("地区选择".equals(title)) {
	//
	// }
}
