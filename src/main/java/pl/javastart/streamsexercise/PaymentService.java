package pl.javastart.streamsexercise;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateTimeProvider dateTimeProvider;

    PaymentService(PaymentRepository paymentRepository, DateTimeProvider dateTimeProvider) {
        this.paymentRepository = paymentRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie rosnąco
     */
    List<Payment> findPaymentsSortedByDateAsc() {
        //throw new RuntimeException("Not implemented");
        return paymentRepository.findAll().stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate))
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie malejąco
     */
    List<Payment> findPaymentsSortedByDateDesc() {
        //throw new RuntimeException("Not implemented");
        return paymentRepository.findAll().stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate).reversed())  // comparator za krótki na wydzielanie
                .collect(Collectors.toList());
    }

    /*
    Wydzieliłem go, jako że jest dość długi i w niemal identycznej formie wystąpił w kodzie 2 razy
     */
    private final Comparator<Payment> paymentsSortedByItemCountAscComparator =
            Comparator.comparing(Payment::getPaymentItems, Comparator.comparingInt(List::size));

    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów rosnąco
     */
    List<Payment> findPaymentsSortedByItemCountAsc() {
        //throw new RuntimeException("Not implemented");
        return paymentRepository.findAll().stream()
                .sorted(paymentsSortedByItemCountAscComparator)
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów malejąco
     */
    List<Payment> findPaymentsSortedByItemCountDesc() {
        //throw new RuntimeException("Not implemented");
        return paymentRepository.findAll().stream()
                .sorted(paymentsSortedByItemCountAscComparator.reversed())
                .collect(Collectors.toList());
    }

    /*
    Poniższą metodę wydzieliłem celem skrócenia lambdy w metodzie kolejnej (getPaymentStreamForGivenMonth)
     */
    private boolean wasInGivenMonth(Payment payment, YearMonth yearMonth) {
        ZonedDateTime paymentZonedDateTime = payment.getPaymentDate();
        YearMonth paymentYearMonth = YearMonth.of(paymentZonedDateTime.getYear(), paymentZonedDateTime.getMonth());
        return paymentYearMonth.equals(yearMonth);
    }

    /*
    Poniższą metodę wydzieliłem jako pośrednią/pomocniczą, ponieważ w tej samej postaci pojawiła się w kodzie chyba 3 razy.
    Skróciło to znacząco ilość kodu.
     */
    private Stream<Payment> getPaymentStreamForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll().stream()
                .filter(payment -> wasInGivenMonth(payment, yearMonth));
    }

    /*
    Znajdź i zwróć płatności dla wskazanego miesiąca
     */
    List<Payment> findPaymentsForGivenMonth(YearMonth yearMonth) {
        //throw new RuntimeException("Not implemented");
        return getPaymentStreamForGivenMonth(yearMonth)
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności dla aktualnego miesiąca
     */
    List<Payment> findPaymentsForCurrentMonth() {
        //throw new RuntimeException("Not implemented");
        YearMonth currentYearMonth = dateTimeProvider.yearMonthNow();
        return getPaymentStreamForGivenMonth(currentYearMonth)
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności dla ostatnich X dni
     */
    List<Payment> findPaymentsForGivenLastDays(int days) {
        //throw new RuntimeException("Not implemented");
        ZonedDateTime now = dateTimeProvider.zonedDateTimeNow();
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentDate().until(now, ChronoUnit.DAYS) < days)
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności z jednym elementem
     */
    Set<Payment> findPaymentsWithOnePaymentItem() {
        //throw new RuntimeException("Not implemented");
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentItems().size() == 1)
                .collect(Collectors.toSet());
    }

    /*
    Poniżej kolejna wydzielona metoda pośrednia/pomocnicza, która przy okazji wykorzystuje wcześniej wydzieloną
    metodę getPaymentStreamForGivenMonth(YearMonth yearMonth)
     */
    private Stream<PaymentItem> getPaymentItemStreamForGivenMonth(YearMonth yearMonth) {
        return getPaymentStreamForGivenMonth(yearMonth)
                .map(Payment::getPaymentItems)
                .flatMap(List::stream);
    }

    /*
    Znajdź i zwróć nazwy produktów sprzedanych w aktualnym miesiącu
     */
    Set<String> findProductsSoldInCurrentMonth() {
        //throw new RuntimeException("Not implemented");
        YearMonth currentMonth = dateTimeProvider.yearMonthNow();
        return getPaymentItemStreamForGivenMonth(currentMonth)
                .map(PaymentItem::getName)
                .collect(Collectors.toSet());
    }

    /*
    Policz i zwróć sumę sprzedaży dla wskazanego miesiąca
     */
    BigDecimal sumTotalForGivenMonth(YearMonth yearMonth) {
        //throw new RuntimeException("Not implemented");
        return getPaymentItemStreamForGivenMonth(yearMonth)
                .map(PaymentItem::getFinalPrice)
                .reduce(new BigDecimal(0), BigDecimal::add);
    }

    /*
    Policz i zwróć sumę przyznanych rabatów dla wskazanego miesiąca
     */
    BigDecimal sumDiscountForGivenMonth(YearMonth yearMonth) {
        //throw new RuntimeException("Not implemented");
        return getPaymentItemStreamForGivenMonth(yearMonth)
                .filter(item -> item.getFinalPrice().compareTo(item.getRegularPrice()) < 0)
                .map(item -> item.getRegularPrice().subtract(item.getFinalPrice()))
                .reduce(new BigDecimal(0), BigDecimal::add);
    }

    /*
    Znajdź i zwróć płatności dla użytkownika z podanym mailem
     */
    List<PaymentItem> getPaymentsForUserWithEmail(String userEmail) {
        //throw new RuntimeException("Not implemented");
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getUser().getEmail().equals(userEmail))
                .map(Payment::getPaymentItems)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private int totalValue(Payment payment) {
        BigDecimal totalValue = new BigDecimal(0);
        for (PaymentItem paymentItem : payment.getPaymentItems()) {
            totalValue = totalValue.add(paymentItem.getFinalPrice());
        }

        return totalValue.intValue();
    }

    /*
    Znajdź i zwróć płatności, których wartość przekracza wskazaną granicę
     */
    Set<Payment> findPaymentsWithValueOver(int value) {
        //throw new RuntimeException("Not implemented");
        return paymentRepository.findAll().stream()
                .filter(payment -> totalValue(payment) > value)
                .collect(Collectors.toSet());
    }
}
