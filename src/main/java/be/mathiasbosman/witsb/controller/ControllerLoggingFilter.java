package be.mathiasbosman.witsb.controller;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!default")
public class ControllerLoggingFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) {
    log.info("Controller logging filter initialized");
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {

    if (servletRequest instanceof HttpServletRequest httpRequest) {
      log.trace("Request: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }
}
