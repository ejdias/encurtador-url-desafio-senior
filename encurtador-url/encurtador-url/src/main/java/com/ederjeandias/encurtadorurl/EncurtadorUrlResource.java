package com.ederjeandias.encurtadorurl;

import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RequestMapping("/encurtadorSenior")
@RestController
public class EncurtadorUrlResource {

	@Autowired
	StringRedisTemplate redisTemplate;

	@PostMapping
	public String create(@RequestBody String url) {
		UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" });

		if (urlValidator.isValid(url)) {
			String id = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
			redisTemplate.opsForValue().set(id, url);
			return "URL encurtada gerada com sucesso!\nID: " + id;
		}

		throw new RuntimeException("URL Invalid: " + url);

	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<URL> find(@PathVariable String id) throws URISyntaxException {
		String redirectTo = redisTemplate.opsForValue().get(id);
		URI uri = new URI(redirectTo);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setLocation(uri);
		return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
	}
}
