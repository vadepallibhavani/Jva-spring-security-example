package io.example.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.example.api.data.AuthorTestDataFactory;
import io.example.api.data.BookTestDataFactory;
import io.example.domain.dto.AuthorView;
import io.example.domain.dto.BookView;
import io.example.domain.dto.EditAuthorRequest;
import io.example.domain.dto.ListResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static io.example.util.JsonHelper.fromJson;
import static io.example.util.JsonHelper.toJson;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TestAuthorApi extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthorTestDataFactory authorTestDataFactory;
    @Autowired
    private BookTestDataFactory bookTestDataFactory;

    @Test @WithMockUser
    public void testCreateSuccess() throws Exception {
        EditAuthorRequest goodRequest = new EditAuthorRequest();
        goodRequest.setFullName("Test Author A");

        MvcResult createResult = this.mockMvc
                .perform(post("/api/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, goodRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthorView authorView = fromJson(objectMapper, createResult.getResponse().getContentAsString(), AuthorView.class);
        assertNotNull(authorView.getId(), "Author id must not be null!");
        assertEquals(goodRequest.getFullName(), authorView.getFullName(), "Author name update isn't applied!");
    }

    @Test @WithMockUser
    public void testCreateFail() throws Exception {
        EditAuthorRequest badRequest = new EditAuthorRequest();

        this.mockMvc
                .perform(post("/api/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Method argument validation failed")));
    }

    @Test @WithMockUser
    public void testEditSuccess() throws Exception {
        AuthorView authorView = authorTestDataFactory.createAuthor(mockMvc, "Test Author A");

        EditAuthorRequest updateRequest = new EditAuthorRequest();
        updateRequest.setFullName("Test Author B");
        updateRequest.setAbout("Cool author");

        MvcResult updateResult = this.mockMvc
                .perform(put(String.format("/api/author/%s", authorView.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, updateRequest)))
                .andExpect(status().isOk())
                .andReturn();
        AuthorView newAuthorView = fromJson(objectMapper, updateResult.getResponse().getContentAsString(), AuthorView.class);

        assertEquals(updateRequest.getFullName(), newAuthorView.getFullName(), "Author name update isn't applied!");
        assertEquals(updateRequest.getAbout(), newAuthorView.getAbout(), "Author name update isn't applied!");
    }

    @Test @WithMockUser
    public void testEditFailBadRequest() throws Exception {
        AuthorView authorView = authorTestDataFactory.createAuthor(mockMvc, "Test Author A");

        EditAuthorRequest updateRequest = new EditAuthorRequest();

        this.mockMvc
                .perform(put(String.format("/api/author/%s", authorView.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Method argument validation failed")));
    }

    @Test @WithMockUser
    public void testEditFailNotFound() throws Exception {
        EditAuthorRequest updateRequest = new EditAuthorRequest();
        updateRequest.setFullName("Test Author B");

        this.mockMvc
                .perform(put(String.format("/api/author/%s", "5f07c259ffb98843e36a2aa9"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(objectMapper, updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Entity Author with id 5f07c259ffb98843e36a2aa9 not found")));
    }

    @Test @WithMockUser
    public void testDeleteSuccess() throws Exception {
        AuthorView authorView = authorTestDataFactory.createAuthor(mockMvc, "Test Author A");

        this.mockMvc
                .perform(delete(String.format("/api/author/%s", authorView.getId())))
                .andExpect(status().isOk());

        this.mockMvc
                .perform(get(String.format("/api/author/%s", authorView.getId())))
                .andExpect(status().isNotFound());
    }

    @Test @WithMockUser
    public void testDeleteFailNotFound() throws Exception {
        this.mockMvc
                .perform(delete(String.format("/api/author/%s", "5f07c259ffb98843e36a2aa9")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Entity Author with id 5f07c259ffb98843e36a2aa9 not found")));
    }

    @Test @WithMockUser
    public void testGetSuccess() throws Exception {
        AuthorView authorView = authorTestDataFactory.createAuthor(mockMvc, "Test Author A");

        MvcResult getResult = this.mockMvc
                .perform(get(String.format("/api/author/%s", authorView.getId())))
                .andExpect(status().isOk())
                .andReturn();

        AuthorView newAuthorView = fromJson(objectMapper, getResult.getResponse().getContentAsString(), AuthorView.class);

        assertEquals(authorView.getId(), newAuthorView.getId(), "Author ids must be equal!");
    }

    @Test @WithMockUser
    public void testGetNotFound() throws Exception {
        this.mockMvc
                .perform(get(String.format("/api/author/%s", "5f07c259ffb98843e36a2aa9")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Entity Author with id 5f07c259ffb98843e36a2aa9 not found")));
    }

    @Test @WithMockUser
    public void testGetAuthorBooksSuccess() throws Exception {
        AuthorView authorView = authorTestDataFactory.createAuthor(mockMvc, "Test Author A");
        BookView bookView1 = bookTestDataFactory.createBook(mockMvc, List.of(authorView.getId()), "Test Book A");
        BookView bookView2 = bookTestDataFactory.createBook(mockMvc, List.of(authorView.getId()), "Test Book B");

        MvcResult getBooksResult = this.mockMvc
                .perform(get(String.format("/api/author/%s/book", authorView.getId())))
                .andExpect(status().isOk())
                .andReturn();

        ListResponse<BookView> bookViewList = fromJson(objectMapper,
                getBooksResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertEquals(2, bookViewList.getItems().size(), "Author must have 2 books");
        assertEquals(bookView1.getTitle(), bookViewList.getItems().get(0).getTitle(), "Book title mismatch!");
        assertEquals(bookView2.getTitle(), bookViewList.getItems().get(1).getTitle(), "Book title mismatch!");
    }

}
