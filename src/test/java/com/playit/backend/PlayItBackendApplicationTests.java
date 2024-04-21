package com.playit.backend;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PlayItBackendApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Test
	void getEntryPoint() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("Incroyable")));
	}

	@Test
	void getHelloWorld() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/hello"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("Hello World!")));
	}

	@Test
	void getHelloWorldWithParameter() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/hello?name=Martin"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("Hello Martin!")));
	}

}
