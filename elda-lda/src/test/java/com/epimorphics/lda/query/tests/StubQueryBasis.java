package com.epimorphics.lda.query.tests;

import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.lda.query.APIQuery.QueryBasis;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.textsearch.TextSearchConfig;

public class StubQueryBasis implements QueryBasis {

	private final ShortnameService sns;

	private final TextSearchConfig textSearchConfig = new TextSearchConfig();

	public StubQueryBasis(ShortnameService sns) {
		this.sns = sns;
	}

	@Override public final ShortnameService sns() {
		return sns;
	}

	@Override public final String getDefaultLanguage() {
		return null;
	}

	@Override public String getItemTemplate() {
		return null;
	}

	@Override public final int getMaxPageSize() {
		return QueryParameter.MAX_PAGE_SIZE;
	}

	@Override public final int getDefaultPageSize() {
		return QueryParameter.DEFAULT_PAGE_SIZE;
	}

	@Override public boolean isItemEndpoint() {
		return false;
	}

	@Override public TextSearchConfig getTextSearchConfig() {
		return textSearchConfig;
	}

	@Override public Boolean getEnableCounting() {
		return Boolean.FALSE;
	}

	@Override public long getCacheExpiryMilliseconds() {
		return 5 * 1000;
	}

	@Override public String getGraphTemplate() {
		return null;
	}
}