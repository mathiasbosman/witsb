package be.mathiasbosman.witsb.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import be.mathiasbosman.fs.service.FileService;
import be.mathiasbosman.witsb.WitsbApplicationTest;
import be.mathiasbosman.witsb.entity.Item;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class ItemControllerTest extends WitsbApplicationTest {

  @MockBean
  private FileService fileService;

  private static String map(String path) {
    return ItemController.REQUEST_MAPPING + path;
  }

  @Test
  void create() throws Exception {
    MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt",
        "text/plain", "content".getBytes());
    given(fileService.exists(any())).willReturn(true);
    mvc().perform(multipart(map("/item")).file(multipartFile))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("test.txt"))
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  void delete() throws Exception {
    Item item = create(Item.builder().name("test.txt").build());
    given(fileService.exists(any())).willReturn(false);
    mvc().perform(MockMvcRequestBuilders.delete(map("/item/" + item.getId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(item.getName()))
        .andExpect(jsonPath("$.id").value(item.getId().toString()));
  }

  @Test
  void load() throws Exception {
    Item item = create(Item.builder().name("test.txt").build());
    given(fileService.exists(any())).willReturn(true);
    given(fileService.open(item.getId().toString()))
        .willReturn(IOUtils.toInputStream("test content", StandardCharsets.UTF_8));
    mvc().perform(get(map("/item/" + item.getId())))
        .andExpect(status().isOk());
  }
}
