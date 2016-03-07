package rover.utils;

import com.google.inject.Singleton;
import rover.services.GeoService;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eq on 06/03/16.
 */
@Singleton
public class CountryFrequencyFilter implements Filter{
    private final long COUNTED_PERIOD = 5000;

    private final GeoService geoService;
    private final String ERROR_JSON = "{\"error\": \"YOU SHALL NOT PASS [try later]\"}";

    private Map<String, Date> dictionary;

    @Inject
    public CountryFrequencyFilter(GeoService geoService){
        this.geoService = geoService;
        dictionary = new HashMap<>();
    }

    @Override public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        if(req.getMethod().equalsIgnoreCase("POST")) {
            String iso = geoService.getCountryByIp(req.getRemoteAddr());
            Date lastPOST = dictionary.get(iso);
            dictionary.put(iso, new Date());

            if(lastPOST != null &&
                lastPOST.getTime() - dictionary.get(iso).getTime() < COUNTED_PERIOD) {
                HttpServletResponse res = (HttpServletResponse) servletResponse;
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                res.setContentType("application/json");
                PrintWriter out = res.getWriter();
                out.print(ERROR_JSON);
                out.flush();
                return; //ha-haa
            }

        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override public void destroy() {}

}
