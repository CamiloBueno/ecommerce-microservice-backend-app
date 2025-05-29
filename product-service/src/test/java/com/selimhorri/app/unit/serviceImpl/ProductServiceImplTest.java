package com.selimhorri.app.unit.serviceImpl;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.helper.ProductMappingHelper;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    private Product product;
    private ProductDto productDto;

    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .imageUrl("electronics.jpg")
                .build();

        categoryDto = CategoryDto.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .imageUrl("electronics.jpg")
                .build();

        product = Product.builder()
                .productId(1)
                .productTitle("Test Product")
                .imageUrl("url.jpg")
                .sku("SKU123")
                .priceUnit(100.0)
                .quantity(5)
                .category(category)
                .build();

        productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Test Product")
                .imageUrl("url.jpg")
                .sku("SKU123")
                .priceUnit(100.0)
                .quantity(5)
                .categoryDto(categoryDto)
                .build();
    }

    @Test
    void testFindAll() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        List<ProductDto> result = productService.findAll();
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getProductTitle());
    }

    @Test
    void testFindByIdFound() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        ProductDto result = productService.findById(1);
        assertEquals("Test Product", result.getProductTitle());
    }

    @Test
    void testFindByIdNotFound() {
        when(productRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productService.findById(1));
    }

    @Test
    void testSave() {
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDto result = productService.save(productDto);
        assertEquals("Test Product", result.getProductTitle());
    }

    @Test
    void testUpdate() {
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDto result = productService.update(productDto);
        assertEquals("Test Product", result.getProductTitle());
    }

    @Test
    void testUpdateById() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductDto result = productService.update(1, productDto);
        assertEquals("Test Product", result.getProductTitle());
    }

    @Test
    void testDeleteById() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));
        assertDoesNotThrow(() -> productService.deleteById(1));
    }
}
