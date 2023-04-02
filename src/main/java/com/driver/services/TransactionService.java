package com.driver.services;

import com.driver.models.Book;
import com.driver.models.Card;
import com.driver.models.CardStatus;
import com.driver.models.Transaction;
import com.driver.models.TransactionStatus;
import com.driver.repositories.BookRepository;
import com.driver.repositories.CardRepository;
import com.driver.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    @Autowired
    BookRepository bookRepository5;

    @Autowired
    CardRepository cardRepository5;

    @Autowired
    TransactionRepository transactionRepository5;

    @Value("${books.max_allowed}")
    public int max_allowed_books;

    @Value("${books.max_allowed_days}")
    public int getMax_allowed_days;

    @Value("${books.fine.per_day}")
    public int fine_per_day;

    public String issueBook(int cardId, int bookId) throws Exception {
        //check whether bookId and cardId already exist
        //conditions required for successful transaction of issue book:
        //1. book is present and available
        // If it fails: throw new Exception("Book is either unavailable or not present");
        //2. card is present and activated
        // If it fails: throw new Exception("Card is invalid");
        //3. number of books issued against the card is strictly less than max_allowed_books
        // If it fails: throw new Exception("Book limit has reached for this card");
        //If the transaction is successful, save the transaction to the list of transactions and return the id

        //Note that the error message should match exactly in all cases
        Transaction txn = new Transaction();
        txn.setTransactionStatus(TransactionStatus.FAILED);
        txn.setIssueOperation(true);
        Book book;
        try{
            book = bookRepository5.findById(bookId).get();
        } catch(Exception e) {
            transactionRepository5.save(txn);
            throw new Exception("Book is either unavailable or not present");
        }
        
        if(!book.isAvailable()) {
            transactionRepository5.save(txn);
            throw new Exception("Book is either unavailable or not present");
        }
        
        txn.setBook(book);
        Card card;
        try{
            card = cardRepository5.findById(cardId).get();
        } catch(Exception e) {
            transactionRepository5.save(txn);
            throw new Exception("Card is invalid");
        }
        
        txn.setCard(card);
        
        if(card.getCardStatus()==CardStatus.DEACTIVATED) {
            transactionRepository5.save(txn);
            throw new Exception("Card is invalid");
        }
        
        if(max_allowed_books>=card.getBooks().size()) {
            transactionRepository5.save(txn);
            throw new Exception("Book limit has reached for this card");
        } 

        
       txn.setTransactionStatus(TransactionStatus.SUCCESSFUL);
       book.getTransactions().add(txn);
       card.getBooks().add(book);
       cardRepository5.save(card);
       bookRepository5.save(book);
       return txn.getId()+""; //return transactionId instead
    }

    public Transaction returnBook(int cardId, int bookId) throws Exception{

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId, TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size() - 1);

        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
        //make the book available for other users
        //make a new transaction for return book which contains the fine amount as well

        Transaction returnBookTransaction  = null;
        return returnBookTransaction; //return the transaction after updating all details
    }
}
