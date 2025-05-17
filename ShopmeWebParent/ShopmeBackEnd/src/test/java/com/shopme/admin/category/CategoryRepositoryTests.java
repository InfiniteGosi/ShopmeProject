package com.shopme.admin.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;


import com.shopme.common.entity.Category;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class CategoryRepositoryTests {

	@Autowired
	private CategoryRepository repo;
	
	@Test
	public void testCreateRootCategory() {
		Category computers = new Category("Computers");
		Category electronics = new Category("Electronics");
		
		repo.saveAll(List.of(computers, electronics));
	}
	
	@Test
	public void testCreateSubCategory1() {
		Category parent = new Category(1);
		Category laptops = new Category("Laptops", parent);
		Category desktops = new Category("Desktops", parent);
		Category components = new Category("Computer Components", parent);
		repo.saveAll(List.of(laptops, desktops, components));
	}
	
	@Test
	public void testCreateSubCategory2() {
		Category parent = new Category(2);
		Category cameras = new Category("Cameras", parent);
		Category phones = new Category("Smartphones", parent);
		repo.saveAll(List.of(cameras, phones));
	}
	
	@Test
	public void testCreateSubCategory3() {
		Category parent = new Category(5);
		Category memory = new Category("Memory", parent);
		Category saved = repo.save(memory);
		
		assertThat(saved.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testGetCategory() {
		Category category = repo.findById(2).get();
		System.out.println(category.getName());

		Set<Category> children = category.getChildren();

		for (Category subCategory : children) {
			System.out.println(subCategory.getName());	
		}

		assertThat(children.size()).isGreaterThan(0);
	}
	
	@Test
	public void testPrintHierarchicalCategories() {
		Iterable<Category> categories = repo.findAll();

		for (Category category : categories) {
			if (category.getParent() == null) {
				System.out.println(category.getName());

				Set<Category> children = category.getChildren();

				for (Category subCategory : children) {
					System.out.println("--" + subCategory.getName());
					printChildren(subCategory, 1);
				}
			}
		}
	}
	
	private void printChildren(Category parent, int subLevel) {
		int newSubLevel = subLevel + 1;
		Set<Category> children = parent.getChildren();

		for (Category subCategory : children) {
			for (int i = 0; i < newSubLevel; i++) {				
				System.out.print("--");
			}

			System.out.println(subCategory.getName());

			printChildren(subCategory, newSubLevel);
		}		
	}
	
	
}