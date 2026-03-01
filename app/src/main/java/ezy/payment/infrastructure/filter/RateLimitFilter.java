package ezy.payment.infrastructure.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    
    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private static final long WINDOW_SIZE_MS = TimeUnit.MINUTES.toMillis(1);
    
    private final ConcurrentHashMap<String, RequestWindow> requestCounts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIp(httpRequest);
        
        cleanupExpiredEntries();
        
        if (isRateLimitExceeded(clientIp)) {
            logger.warn("Rate limit exceeded for IP: {}", clientIp);
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"error\":\"Rate limit exceeded. Maximum " + MAX_REQUESTS_PER_MINUTE + 
                " requests per minute allowed. Please try again later.\"}");
            return;
        }
        
        recordRequest(clientIp);
        
        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private boolean isRateLimitExceeded(String clientIp) {
        RequestWindow window = requestCounts.get(clientIp);
        
        if (window == null) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        
        if (now - window.windowStart > WINDOW_SIZE_MS) {
            return false;
        }
        
        return window.requestCount >= MAX_REQUESTS_PER_MINUTE;
    }

    private void recordRequest(String clientIp) {
        long now = System.currentTimeMillis();
        
        requestCounts.compute(clientIp, (ip, window) -> {
            if (window == null || now - window.windowStart > WINDOW_SIZE_MS) {
                return new RequestWindow(now, 1);
            } else {
                window.requestCount++;
                return window;
            }
        });
    }

    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        requestCounts.entrySet().removeIf(entry -> 
            now - entry.getValue().windowStart > WINDOW_SIZE_MS);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Rate limit filter initialized: {} requests per minute", MAX_REQUESTS_PER_MINUTE);
    }

    @Override
    public void destroy() {
        requestCounts.clear();
    }

    private static class RequestWindow {
        long windowStart;
        int requestCount;

        RequestWindow(long windowStart, int requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}
