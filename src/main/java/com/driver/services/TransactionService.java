package com.driver.services;

import com.driver.models.Book;
import com.driver.models.Card;
import com.driver.models.CardStatus;
import com.driver.models.Transaction;
import com.driver.models.TransactionStatus;
import com.driver.repositories.BookRepository;
import com.driver.repositories.CardRepository;
import com.driver.repositories.TransactionRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
        Book book = bookRepository5.findById(bookId).get();
        Card card = cardRepository5.findById(cardId).get();
        
        Transaction txn = new Transaction();
        txn.setBook(book);
        txn.setCard(card);
        txn.setIssueOperation(true);
        
        if(book==null || !book.isAvailable()) {
            txn.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(txn);
            throw new Exception("Book is either unavailable or not present");
        }
        
        if(card==null || card.getCardStatus().equals(CardStatus.DEACTIVATED)) {
            txn.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(txn);
            throw new Exception("Card is invalid");
        }
        
        if(max_allowed_books <= card.getBooks().size()) {
            txn.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(txn);
            throw new Exception("Book limit has reached for this card");
        }
        
        book.setCard(card);
        card.getBooks().add(book);
        book.setAvailable(false);
        cardRepository5.save(card);
        bookRepository5.updateBook(book);
        txn.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        return txn.getTransactionId();
    }

    public Transaction returnBook(int cardId, int bookId) throws Exception{

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId, TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size() - 1);
        
        Date issueDate = transaction.getTransactionDate();
        long timeIssueTime = Math.abs(System.currentTimeMillis()-issueDate.getTime());
        long no_of_days = TimeUnit.DAYS.convert(timeIssueTime, TimeUnit.MILLISECONDS);
        
        int fine = 0;
        if(no_of_days > getMax_allowed_days) {
            fine = (int)((no_of_days - getMax_allowed_days) * fine_per_day);
        }
        
        Book book = transaction.getBook();
        book.setAvailable(true);
        book.setCard(null);
        bookRepository5.updateBook(book);
        
        Transaction txn = new Transaction();
        txn.setBook(book);
        txn.setCard(transaction.getCard());
        txn.setFineAmount(fine);
        txn.setIssueOperation(false);
        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
        //make the book available for other users
        //make a new transaction for return book which contains the fine amount as well
        txn.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        return txn; //return the transaction after updating all details
    }
}
