package com.zhao.easyJmeter.service;

import com.zhao.easyJmeter.dto.book.CreateOrUpdateBookDTO;
import com.zhao.easyJmeter.model.BookDO;

import java.util.List;

/**
 * @author pedro@TaleLin
 * @author Juzi@TaleLin
 */
public interface BookService {

    boolean createBook(CreateOrUpdateBookDTO validator);

    List<BookDO> getBookByKeyword(String q);

    boolean updateBook(BookDO book, CreateOrUpdateBookDTO validator);

    BookDO getById(Integer id);

    List<BookDO> findAll();

    boolean deleteById(Integer id);

    List<BookDO> getBookByTitleKey(String q);
}
