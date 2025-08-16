package com.icbt.pahanaeduonlinebillingsystem.bill.servlet;

import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDTO;
import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDetailDTO;
import com.icbt.pahanaeduonlinebillingsystem.bill.mapper.BillMapper;
import com.icbt.pahanaeduonlinebillingsystem.bill.service.BillService;
import com.icbt.pahanaeduonlinebillingsystem.bill.service.impl.BillServiceImpl;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.SendResponse;
import com.icbt.pahanaeduonlinebillingsystem.common.util.ServletUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet(name = "BillServlet", urlPatterns = "/bills")
public class BillServlet extends HttpServlet {

    private BillService billService;
    private static final Logger LOGGER = LogUtil.getLogger(BillServlet.class);
    private static final int INITIAL_ADMIN_ID = 1;

    @Override
    public void init() {
        billService = new BillServiceImpl();
    }


    private BillDTO parseBillFormBody(HttpServletRequest req) {
        try {
            BillDTO billDTO = new BillDTO();
            billDTO.setCustomerId(Integer.parseInt(req.getParameter("customerId")));

            List<BillDetailDTO> details = new ArrayList<>();
            String[] itemIds = req.getParameterValues("item_id");
            String[] units = req.getParameterValues("units");

            if (itemIds != null && units != null && itemIds.length == units.length) {
                for (int i = 0; i < itemIds.length; i++) {
                    BillDetailDTO detail = new BillDetailDTO();
                    detail.setItemId(Integer.parseInt(itemIds[i]));
                    detail.setUnits(Integer.parseInt(units[i]));
                    details.add(detail);
                }
            } else {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.INVALID_BILL_INPUTS);
            }
            billDTO.setDetails(details);
            return billDTO;

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid number format in bill form data: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.INVALID_BILL_INPUTS);
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer currentUserId = ServletUtil.getUserIdFromSession(req);

        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        try {
            BillDTO billDTO = parseBillFormBody(req);

            Map<String, String> errors = Validator.billValidate(billDTO);
            if (!errors.isEmpty()) {
                LOGGER.log(Level.WARNING, "Bill validation failed: " + errors);
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid bill data.", "errors", errors));
                return;
            }

            BillDTO generatedBill = billService.generateBill(billDTO, currentUserId);
            SendResponse.sendJson(resp, HttpServletResponse.SC_CREATED, Map.of("message", "Bill generated successfully", "bill", BillMapper.toToMap(generatedBill)));
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.WARNING, "Business error during bill generation: " + e.getExceptionType().name() + " - " + e.getMessage());
            int statusCode;
            switch (e.getExceptionType()) {
                case ITEM_NOT_FOUND:
                case CUSTOMER_NOT_FOUND:
                case INSUFFICIENT_STOCK:
                    statusCode = HttpServletResponse.SC_NOT_FOUND;
                    break;
                default:
                    statusCode = HttpServletResponse.SC_BAD_REQUEST;
                    break;
            }
            SendResponse.sendJson(resp, statusCode, Map.of("message", e.getMessage()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during POST /bills (generate bill): " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during bill generation."));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer currentUserId = ServletUtil.getUserIdFromSession(req);

        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        try {
            String idStr = req.getParameter("id");
            String action = req.getParameter("action");

            if (idStr != null && !idStr.trim().isEmpty()) {
                Integer billId = Integer.parseInt(idStr);
                BillDTO billDTO = billService.getBillById(billId);

                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, BillMapper.toToMap(billDTO));

            } else {
                Map<String, String> searchParams = new HashMap<>();
                String searchTerm = req.getParameter("search");
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    searchParams.put("search", searchTerm);
                }

                List<BillDTO> bills = billService.getAll(searchParams);

                List<Map<String, Object>> billMaps = bills.stream()
                        .map(BillMapper::toToMap)
                        .collect(Collectors.toList());

                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, billMaps);
            }
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.WARNING, "Business error during GET /bills: " + e.getExceptionType().name() + " - " + e.getMessage());
            int statusCode = (e.getExceptionType() == ExceptionType.BILL_NOT_FOUND) ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_BAD_REQUEST;
            SendResponse.sendJson(resp, statusCode, Map.of("message", e.getMessage()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during GET /bills: " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error while fetching bills."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String currentUserRole = ServletUtil.getUserRoleFromSession(req);
        Integer currentUserId = ServletUtil.getUserIdFromSession(req);

        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        if (!"ADMIN".equals(currentUserRole) || !currentUserId.equals(INITIAL_ADMIN_ID)) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Forbidden: Only the Initial Admin can delete bills."));
            return;
        }

        try {
            String idStr = req.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Bill ID is required for deletion."));
                return;
            }

            Integer billId = Integer.parseInt(idStr);
            boolean isDeleted = billService.delete(currentUserId, billId);
            if (isDeleted) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Bill deleted successfully."));
            } else {
                SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Bill deletion failed unexpectedly."));
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid bill ID format for deletion: " + e.getMessage());
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid bill ID format."));
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.WARNING, "Business error during bill delete: " + e.getExceptionType().name() + " - " + e.getMessage());
            int statusCode = (e.getExceptionType() == ExceptionType.BILL_NOT_FOUND) ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_BAD_REQUEST;
            SendResponse.sendJson(resp, statusCode, Map.of("message", e.getMessage()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during DELETE /bills: " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during bill deletion."));
        }
    }
}