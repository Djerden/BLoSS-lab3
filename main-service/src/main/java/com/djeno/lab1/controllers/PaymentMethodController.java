package com.djeno.lab1.controllers;

import com.djeno.lab1.persistence.DTO.payment.AddCardRequest;
import com.djeno.lab1.persistence.DTO.payment.PaymentCardDTO;
import com.djeno.lab1.persistence.models.PaymentMethod;
import com.djeno.lab1.services.PaymentMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Validated
@Tag(name = "Взаимодействие с картами")
@RequiredArgsConstructor
@RequestMapping("/cards")
@RestController
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @Operation(
            summary = "Добавить новую карту",
            description = "Добавляет новую платежную карту для текущего пользователя",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта успешно добавлена"),
            @ApiResponse(responseCode = "400", description = "Неверные данные карты"),
            @ApiResponse(responseCode = "403", description = "Требуется авторизация")
    })
    @PreAuthorize("hasAuthority('ADD_PAYMENT_METHOD')")
    @PostMapping()
    public ResponseEntity<String> addCard(
            @Parameter(description = "Данные карты", required = true)
            @RequestBody
            @Valid
            AddCardRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors.toString());
        }

        paymentMethodService.addCard(request);
        return ResponseEntity.ok("Карта добавлена");
    }

    @Operation(
            summary = "Сделать карту основной",
            description = "Устанавливает указанную карту как основную для оплаты",
            security = @SecurityRequirement(name = "bearerAuth"),
            parameters = {
                    @Parameter(name = "id", description = "ID карты", required = true, example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта установлена как основная"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Требуется авторизация")
    })
    @PreAuthorize("hasAuthority('SET_PRIMARY_PAYMENT_METHOD')")
    @PatchMapping("/{id}/primary")
    public ResponseEntity<String> setPrimaryCard(@PathVariable Long id) {
        paymentMethodService.setPrimaryCard(id);
        return ResponseEntity.ok("Основная карта изменена");
    }

    @Operation(
            summary = "Получить список карт",
            description = "Возвращает список всех карт текущего пользователя",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список карт получен"),
            @ApiResponse(responseCode = "403", description = "Требуется авторизация")
    })
    @PreAuthorize("hasAuthority('VIEW_PAYMENT_METHOD')")
    @GetMapping()
    public ResponseEntity<List<PaymentCardDTO>> getUserCards() {
        List<PaymentCardDTO> cards = paymentMethodService.getUserCards();
        return ResponseEntity.ok(cards);
    }

    @Operation(
            summary = "Удалить карту",
            description = "Удаляет карту по ID",
            security = @SecurityRequirement(name = "bearerAuth"),
            parameters = {
                    @Parameter(name = "id", description = "ID карты", required = true, example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Требуется авторизация")
    })
    @PreAuthorize("hasAuthority('DELETE_PAYMENT_METHOD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCard(@PathVariable Long id) {
        paymentMethodService.deleteCard(id);
        return ResponseEntity.ok("Карта успешно удалена");
    }
}
