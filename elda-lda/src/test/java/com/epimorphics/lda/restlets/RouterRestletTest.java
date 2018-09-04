package com.epimorphics.lda.restlets;

import com.epimorphics.util.URIUtils;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RouterRestletTest {
	@Test
	public void makeRequestURI_WithoutBase_WithoutForward_ReturnsRequestUri() {
		URI result = new RequestURIScenario().run();
		assertEquals("http://test.org/request", result.toString());
	}

	@Test
	public void makeRequestURI_WithoutBase_WithForward_ReturnsForwardUri() {
		URI result = new RequestURIScenario()
				.withForward("https", "proxy.net")
				.run();
		assertEquals("https://proxy.net/request", result.toString());
	}

	@Test
	public void makeRequestURI_WithoutBase_WithForwardPort_ReturnsForwardUri() {
		URI result = new RequestURIScenario()
				.withForward("https", "proxy.net:9090")
				.run();
		assertEquals("https://proxy.net:9090/request", result.toString());
	}

	@Test
	public void makeRequestURI_WithAbsoluteBase_WithoutForward_ReturnsBaseUri() {
		URI result = new RequestURIScenario()
				.withBase("https://proxy.net")
				.run();
		assertEquals("https://proxy.net/request", result.toString());
	}

	@Test
	public void makeRequestURI_WithAbsoluteBase_WithForward_ReturnsBaseUri() {
		URI result = new RequestURIScenario()
				.withBase("https://proxy.net")
				.withForward("ftp", "forward.com")
				.run();
		assertEquals("https://proxy.net/request", result.toString());
	}

	@Test
	public void makeRequestURI_WithRelativeBase_WithoutForward_ReturnsRequestUri() {
		URI result = new RequestURIScenario()
				.withBase("/test/path")
				.run();
		assertEquals("http://test.org/test/path/request", result.toString());
	}

	@Test
	public void makeRequestURI_WithRelativeBasePath_WithForward_ReturnsForwardUri() {
		URI result = new RequestURIScenario()
				.withBase("/test/path")
				.withForward("https", "proxy.net")
				.run();
		assertEquals("https://proxy.net/test/path/request", result.toString());
	}

	@Test
	public void makeRequestURI_WithRelativeBaseHost_WithForward_ReturnsForwardUri() {
		URI result = new RequestURIScenario()
				.withBase("//proxy.net/test/path")
				.withForward("https", "forward.com")
				.run();
		assertEquals("https://proxy.net/test/path/request", result.toString());
	}

	@Test
	public void makeRequestURI_WithRelativeBaseHost_WithForwardProtoOnly_ReturnsForwardUri() {
		URI result = new RequestURIScenario()
				.withBase("/test/path")
				.withForward("https", null)
				.run();
		assertEquals("https://test.org/test/path/request", result.toString());
	}

	class RequestURIScenario {
		private final UriInfo ui = mock(UriInfo.class);
		private String base;
		private URI requestUri = URIUtils.newURI("http://test.org/request");
		private final HttpServletRequest request = mock(HttpServletRequest.class);

		RequestURIScenario() {
			when(ui.getRequestUri()).thenReturn(requestUri);
			when(ui.getPath()).thenReturn("/request");
		}

		RequestURIScenario withBase(String base) {
			this.base = base;
			return this;
		}

		RequestURIScenario withForward(String proto, String host) {
			when(request.getHeader("X-Forwarded-Proto")).thenReturn(proto);
			when(request.getHeader("X-Forwarded-Host")).thenReturn(host);
			return this;
		}

		URI run() {
			return RouterRestlet.makeRequestURI(ui, base, request);
		}
	}
}