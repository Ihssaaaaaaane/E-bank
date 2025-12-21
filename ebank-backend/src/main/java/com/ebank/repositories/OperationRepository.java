package com.ebank.repositories;

import com.ebank.entities.BankAccount;
import com.ebank.entities.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepository extends JpaRepository<Operation, Long> {

    /**
     * Recherche les opérations pour un compte donné, paginées et triées par date (descendant).
     * Utilisé pour obtenir les 10 dernières opérations et la pagination.
     */
    Page<Operation> findByBankAccountOrderByDateDesc(BankAccount bankAccount, Pageable pageable);
    Page<Operation> findByBankAccount(BankAccount bankAccount, Pageable pageable);
}