package com.bossteach.job.taobao;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.bossteach.job.CrawlTaoBaoJob;
import com.bossteach.job.baidu.GetThread;
import com.bossteach.model.HongKangInsuranceTransaction;


public class CrawlHongKangInsuranceTransaction extends CrawlTaoBaoJob {
	private Log logger = LogFactory.getLog(CrawlHongKangInsuranceTransaction.class);
	private static final String PROXY_HOST= "10.18.8.108";
	private static final int PROXY_PORT = 8008;
	private static final String PROXY_USERNAME= "niyong";
	private static final String PROXY_PASSWORD= "nY111111";
	private static final String PROXY_WORKSTATION= "isa06";
	private static final String PROXY_DOMAIN= "ulic";
	private static String HongKangTransactionUrl = "http://baoxian.taobao.com/json/PurchaseList.do";	
 
	public DefaultHttpClient getHttpClient(DefaultHttpClient httpClient){
        NTCredentials credentials = new NTCredentials(PROXY_USERNAME ,PROXY_PASSWORD , PROXY_WORKSTATION, PROXY_DOMAIN);	        
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(PROXY_HOST, PROXY_PORT), credentials);	      
        HttpHost proxy = new HttpHost(PROXY_HOST, PROXY_PORT);
        httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        return httpClient;
    }	
	
	/** 
	     * Receive Html By Url  
	     * @param url 
	     * @return 
	     */  
	    public String getHtmlByUrl(URI uri){
	        StringWriter sw = new StringWriter();
	        
//	        DefaultHttpClient httpClient = getHttpClient(new DefaultHttpClient());//创建httpClient对象                	   
	        DefaultHttpClient httpClient = new DefaultHttpClient();
	        //如果代理需要密码验证，这里设置用户名密码  	        
	        HttpGet httpGet = new HttpGet(uri);	        
	        try {	        	
	            HttpResponse response = httpClient.execute(httpGet);//得到responce对象  
	            int resStatu = response.getStatusLine().getStatusCode();//返回 	 
	            if (resStatu == HttpStatus.SC_OK) {//200正常
	                //获得相应实体  	            	
	                HttpEntity entity = response.getEntity();
	                if (entity!=null) {	             
	                	InputStreamReader insr = new InputStreamReader(entity.getContent(), "gb2312" /*ContentType.getOrDefault(entity).getCharset()*/);
                        IOUtils.copy(insr, sw);
	                	insr.close();
	                }
	            } else {
		            System.out.println("Http Status Code:" + resStatu);
	            }
	        } catch (Exception e) {  
	        	 System.out.println("访问【"+uri+"】出现异常!");
	             e.printStackTrace();  
	        } finally {	        	        	
	        	logger.info("HttpClient连接关闭.");
	            httpClient.getConnectionManager().shutdown();	            
	        }  
	        return sw.toString();  
	    }
	    
	    /**
	     * Analyse Transaction Record Detail
	     * @param html
	     */
	    public List<HongKangInsuranceTransaction> analyseTransactionRecordsHtml(String html){
	    	List<HongKangInsuranceTransaction> hks = new ArrayList<HongKangInsuranceTransaction>();
	    	HongKangInsuranceTransaction tbt = null;
	        if (html!= null && !"".equals(html)) {	        	
	            Document doc = Jsoup.parse(html);  
	            Elements trs = doc.select("tbody tr");
	            for (Element tr: trs) {
	            	tr = tr.html(tr.html().replace("\\r", "").replace("\\t", "").replace("\\n", "").replace("\\/td", "").replace("&lt;", "").replace("&gt;", "").replace("\\/tr", "").replace(" <em>", "").replace("\\/em", "").replace("</em>", "").replace("\\/table", ""));
	            	tbt = new HongKangInsuranceTransaction();
	            	//去掉第一行
	            	if ("".equals(tr.select("td:eq(0)").html())) {
	            		continue;
	            	}
	            	//过滤不是今天的记录
	            	String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	            	if (!currentDate.equals(tr.select("td:eq(4)").html().subSequence(0, 10))) {
	            		break;
	            	}
	            	System.out.println(tr.html());
	            	tbt.setSellerId("1128953583");
	            	tbt.setItemId("17305541936");
	            	tbt.setBuyer(tr.select("td:eq(0)").html());
	            	tbt.setItemName(tr.select("td:eq(1)").html());
	            	tbt.setPremium(Double.parseDouble(tr.select("td:eq(2)").html()));
	            	tbt.setCount(Long.parseLong(tr.select("td:eq(3)").html()));
	            	tbt.setTransactionDate(tr.select("td:eq(4)").html());
	            	tbt.setStatus(tr.select("td:eq(5)").html());
	            	taoBaoTransactionService.createHongKangInsuranceTransaction(tbt);
	            	hks.add(tbt);
	            }
	        } 
	        return hks;
	    }
	    
	    /**
	     * Analyse Transaction Count
	     * @param html
	     */
	    public Long analyseTransactionCountHtml(String html){
	    	Long pageCount =0L;
	        if (html!= null && !"".equals(html)) {	        	
	            Document doc = Jsoup.parse(html);  
	            Elements trs = doc.select("ul.tab-bar  li:not(.sel)");
	            Pattern p = Pattern.compile("m>(.*)件");
	            Matcher m = p.matcher(trs.html());
	            if (m.find()){
	            	pageCount = new Double(Math.ceil(Long.parseLong(m.group().replace("m>", "").replace("件", ""))/10.0)).longValue();
		            System.out.println(m.group().replace("m>", "").replace("件", "") + "件,共" + pageCount + "页");
	            }
	        } 
	        return pageCount;
	    }
	    
	    
	    /**
	     * Pooled Get URL for Paging
	     * @throws Exception
	     */
	    public void pooledGetHtmlByUrl() throws Exception{
	    	SchemeRegistry schemeRegistry = new SchemeRegistry();
	    	schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
	    	ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
//	    	DefaultHttpClient httpClient = getHttpClient(new DefaultHttpClient(cm));
	    	DefaultHttpClient httpClient = new DefaultHttpClient(cm);
	    	
	    	// URIs to perform GETs on
	    	String[] urisToGet = {
	    	   
	    	};

	    	// create a thread for each URI
	    	GetThread[] threads = new GetThread[urisToGet.length];
	    	for (int i = 0; i < threads.length; i++) {
	    	    HttpGet httpGet = new HttpGet(urisToGet[i]);
	    	    threads[i] = new GetThread(httpClient, httpGet);
	    	}

	    	// start the threads
	    	for (int j = 0; j < threads.length; j++) {
	    	    threads[j].start();
	    	}

	    	// join the threads
	    	for (int j = 0; j < threads.length; j++) {
	    	    threads[j].join();
	    	}
	    }
	    
	    /**
	     * Initial Parameter
	     * @return
	     */
	    public URI initUrl(int pageNo){
	    	URIBuilder builder;
	    	URI ret = null;
			try {
				builder = new URIBuilder(HongKangTransactionUrl);
				ret =  builder.addParameter("page", Integer.toString(pageNo))
				 	           .addParameter("itemid", "17305541936")
				 	           .addParameter("sellerId", "1128953583")
				 	           .addParameter("callback", "mycallback")
				 	           .addParameter("sold_total_num","0")
				 	           .addParameter("callback", "mycallback").build();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return ret;
	    }
	    public static void main(String[] args) throws Exception {
	    	String html = null;
	    	
	    	CrawlHongKangInsuranceTransaction job = new CrawlHongKangInsuranceTransaction();
	    	Long pageCount = job.analyseTransactionCountHtml("http://baoxian.taobao.com/item.htm?spm=a220m.1000858.1000725.1.lMZiU8&id=17305541936&is_b=1&cat_id=2&q=%BA%EB%BF%B5&rn=859d895e481ccf0569738ec7a55d28a3");
	    	
	    	List<HongKangInsuranceTransaction> list = new ArrayList<HongKangInsuranceTransaction>();
	    	for (int i = 1; i <= pageCount; i++) {	    		
	    		html = job.getHtmlByUrl(job.initUrl(i));
	    		list.addAll(job.analyseTransactionRecordsHtml(html));
			}
	    	
	    	
//	    	String html = job.getHtmlByUrl("http://baoxian.taobao.com/item.htm?spm=a220m.1000858.1000725.1.lMZiU8&id=17305541936&is_b=1&cat_id=2&q=%BA%EB%BF%B5&rn=859d895e481ccf0569738ec7a55d28a3");
    	
//	    	String html = job.getHtmlByUrl("http://baoxian.taobao.com/json/PurchaseList.do?page=1&itemId=17305541936&sellerId=1128953583&callback=mycallback&sold_total_num=0");
//	    	job.analyseTransactionRecordsHtml(html);
//	    	job.poolRequest();
//	    	job.transformToJsonObject();
		}
}

