package paulfranklin.paulfranklin.practice.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paulfranklin.paulfranklin.practice.dtos.requests.NewReimbursementRequest;
import paulfranklin.paulfranklin.practice.dtos.responses.ReimbursementResponse;
import paulfranklin.paulfranklin.practice.entities.Reimbursement;
import paulfranklin.paulfranklin.practice.entities.User;
import paulfranklin.paulfranklin.practice.exceptions.InvalidReimbursementException;
import paulfranklin.paulfranklin.practice.repositories.ReimbursementRepository;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ReimbursementService {
    private final ReimbursementRepository reimbursementRepository;

    public ReimbursementService(ReimbursementRepository reimbursementRepository) {
        this.reimbursementRepository = reimbursementRepository;
    }

    public List<ReimbursementResponse> getReimbursementsByAuthorId(String authorId) throws SQLException {
        List<Reimbursement> reimbursements;
        try {
            reimbursements = reimbursementRepository.findAllByAuthorUserId(authorId);
        } catch (Exception e) {
            throw new SQLException(e);
        }

        List<ReimbursementResponse> reimbursementResponses = new LinkedList<>();

        reimbursements.forEach(reimbursement -> reimbursementResponses.add(new ReimbursementResponse(reimbursement)));

        return reimbursementResponses;
    }

    public void createNewReimbursement(NewReimbursementRequest req, User user) throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Reimbursement reimbursement = new Reimbursement(UUID.randomUUID().toString(), req.getAmount(), timestamp, null, req.getDescription(), user);
        try {
            reimbursementRepository.save(reimbursement);
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public ReimbursementResponse getReimbursementById(String reimbId) throws SQLException {
        try {
            Optional<Reimbursement> reimbursementOptional = reimbursementRepository.findById(reimbId);
            Reimbursement reimbursement = reimbursementOptional.orElseGet(InvalidReimbursementException::reimbursementNotFound);
            return new ReimbursementResponse(reimbursement);
        } catch (InvalidReimbursementException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public void updateReimbursementResolvedById(String reimbId) throws SQLException {
        Timestamp resolved = new Timestamp(System.currentTimeMillis());
        try {
            reimbursementRepository.updateResolvedById(reimbId, resolved);
        } catch (Exception e) {
            throw new SQLException();
        }
    }
}
