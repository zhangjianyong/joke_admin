package com.doumiao.joke.lang;

import com.doumiao.joke.enums.ArticleType;

public class Article {
	private String title;
	private String content;
	private String pic;
	private String picOri;
	private ArticleType type;
	private String fetchSite;
	private String fetchSitePid;
	private int memberId;
	private int status;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public String getPicOri() {
		return picOri;
	}

	public void setPicOri(String picOri) {
		this.picOri = picOri;
	}

	public ArticleType getType() {
		return type;
	}

	public void setType(ArticleType type) {
		this.type = type;
	}

	public String getFetchSite() {
		return fetchSite;
	}

	public void setFetchSite(String fetchSite) {
		this.fetchSite = fetchSite;
	}

	public String getFetchSitePid() {
		return fetchSitePid;
	}

	public void setFetchSitePid(String fetchSitePid) {
		this.fetchSitePid = fetchSitePid;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Article [title=" + title + ", content=" + content + ", pic="
				+ pic + ", picOri=" + picOri + ", type=" + type
				+ ", fetchSite=" + fetchSite + ", fetchSitePid=" + fetchSitePid
				+ ", memberId=" + memberId + ", status=" + status + "]";
	}

}
