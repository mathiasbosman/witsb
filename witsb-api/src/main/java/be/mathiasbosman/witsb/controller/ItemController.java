package be.mathiasbosman.witsb.controller;

import be.mathiasbosman.witsb.dto.ItemDto;
import be.mathiasbosman.witsb.entity.Item;
import be.mathiasbosman.witsb.service.ItemService;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(ItemController.REQUEST_MAPPING)
public class ItemController {

  public static final String REQUEST_MAPPING = "/rest";
  private final ItemService itemService;

  @ModelAttribute
  public void interceptor(HttpServletRequest request) {
    log.trace("URI {} called by {}", request.getRequestURI(), request.getRemoteAddr());
  }

  @PostMapping("item")
  public ItemDto create(@RequestParam("file") MultipartFile file) throws IOException {
    Item createdItem = itemService.create(file.getOriginalFilename(), file.getInputStream());
    return new ItemDto(createdItem);
  }

  @GetMapping("item/{uuid}")
  public ResponseEntity<Resource> load(@PathVariable String uuid) {
    InputStream itemStream = itemService.load(UUID.fromString(uuid));
    InputStreamResource resource = new InputStreamResource(itemStream);
    return new ResponseEntity<>(resource, HttpStatus.OK);
  }

  @DeleteMapping("item/{uuid}")
  public ItemDto delete(@PathVariable String uuid) {
    Item deletedItem = itemService.delete(UUID.fromString(uuid));
    return new ItemDto(deletedItem);
  }
}
