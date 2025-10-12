package at.fhtw.ctfbackend.services;

import at.fhtw.ctfbackend.entity.CategoryEntity;
import at.fhtw.ctfbackend.external.ConfluenceClient;
import at.fhtw.ctfbackend.models.Category;
import at.fhtw.ctfbackend.models.Challenge;
import at.fhtw.ctfbackend.repository.CategoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class CategoryService {
    private final CategoryRepository repo;
    private final ConfluenceClient confluenceClient;
    private  ObjectMapper objectMapper;

    public CategoryService(CategoryRepository repo, ConfluenceClient confluenceClient) {
        this.repo = repo;
        this.confluenceClient = new ConfluenceClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<Category> listAll() {
        return repo.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    private Category toModel(CategoryEntity e) {
        return new Category(
                e.getId(),
                e.getName(),
                e.getSummary(),
                e.getFileUrl()
        );
    }
    private CategoryEntity toEntity(Category c) {
        return new CategoryEntity(
                c.getId(),
                c.getName(),
                c.getSummary(),
                c.getFileUrl()
        );
    }

    public String createCategory(String body) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(body);

        if (rootNode.isNull()) {
            throw new IllegalArgumentException("Invalid JSON");
        }

        if (rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                String result = handleSingleCategoryNode(node);
                if(!result.equals("OK")){
                    return result;
                }
            }
            return "All categories created successfully.";
        } else {
            String result = handleSingleCategoryNode(rootNode);
            if(!result.equals("OK")){
                return result;
            }
            return "Category created successfully.";
        }
    }

    private String handleSingleCategoryNode(JsonNode jsonNode) {
        if (!jsonNode.has("id")) {
            throw new IllegalArgumentException("Missing id");
        }
        if (!jsonNode.has("name")) {
            throw new IllegalArgumentException("Missing name");
        }
        if (!jsonNode.has("pageId")) {
            throw new IllegalArgumentException("Missing pageId");
        }


        String id = jsonNode.get("id").asText();
        String name = jsonNode.get("name").asText();
        String pageId = jsonNode.get("pageId").asText();

        String summary = confluenceClient.fetchSummaryFromConfluence(pageId);
        String fileUrl = "https://technikum-wien-team-kjev9g23.atlassian.net/wiki/spaces/C/pages/" + pageId;

        Category category = new Category(id, name, summary, fileUrl);
        CategoryEntity categoryEntity = toEntity(category);
        try {
            repo.save(categoryEntity);
        } catch (DataIntegrityViolationException e) {
            return "Category already exists.";
        } catch (Exception e) {
            return "Something went wrong with: " +id+ e.getMessage();
        }
        return "OK";
    }

}

