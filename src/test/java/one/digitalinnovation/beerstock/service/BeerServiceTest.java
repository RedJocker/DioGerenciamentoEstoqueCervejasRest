package one.digitalinnovation.beerstock.service;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.exception.BeerStockLessThanZeroException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = -1L;

    @Mock
    private BeerRepository beerRepository;

    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService;

    @Test
    void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
        // given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);
        BeerDTO wrongBeerDTO = BeerDTOBuilder.builder().name("wrong name").build().toBeerDTO();

        // when
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
        when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

        //then
        BeerDTO createdBeerDTO = beerService.createBeer(expectedBeerDTO);


        //using jupiter.api.Assertions for assertions
        assertEquals(createdBeerDTO, expectedBeerDTO);  // como BeerDTO usa @Data que por sua vez usa @EqualsAndHashCode é possível fazer comparação direta
        assertEquals(createdBeerDTO.getId(), expectedBeerDTO.getId());
        assertEquals(createdBeerDTO.getName(), expectedBeerDTO.getName());
        assertEquals(createdBeerDTO.getQuantity(), expectedBeerDTO.getQuantity());

        Assertions.assertTrue(() -> createdBeerDTO.getQuantity() > 2);
        Assertions.assertNotEquals(createdBeerDTO, wrongBeerDTO);
        // Assertions.assertEquals(createdBeerDTO, wrongBeerDTO, () -> "Erro ao criar cerveja");  // com supplier de mensagem de falha


        // using hamcrest.MatcherAssert for assertions
        assertThat(createdBeerDTO, is(equalTo(expectedBeerDTO)));
        assertThat(createdBeerDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
        assertThat(createdBeerDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
        assertThat(createdBeerDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));

        assertThat(createdBeerDTO.getQuantity(), is(greaterThan(2)));
        assertThat(createdBeerDTO, is(not(equalTo(wrongBeerDTO))));
    }


    @Test
    void whenAlreadyRegisteredBeerInformedThenAnExceptionShouldBeThrown() {
        // given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);
        BeerDTO wrongBeerDTO = BeerDTOBuilder.builder().name("wrong name").build().toBeerDTO();

        // when
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));
        // when(beerRepository.save(duplicatedBeer)).thenReturn(duplicatedBeer); // throws org.mockito.exceptions.misusing.UnnecessaryStubbingException:

        // assert
        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
    }


    @Test
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {
        //given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        //when
        when(beerRepository.findByName(expectedFoundBeer.getName()))
                .thenReturn(Optional.of(expectedFoundBeer));

        //then
        BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());

        //assert
        assertEquals(foundBeerDTO, expectedFoundBeerDTO);

    }

    @Test
    void whenInexistentBeerNameIsGivenThenAnExceptionShouldBeThrown() {
        //given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        //when
        when(beerRepository.findByName(expectedFoundBeer.getName()))
                .thenReturn(Optional.empty());

        //assert
        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeer.getName()));

    }

    @Test
    void whenListBeerIsCalledThenReturnAListOfBeers () {
        //given
        BeerDTO beerDTO1 = BeerDTOBuilder.builder().build().toBeerDTO();
        BeerDTO beerDTO2 = BeerDTOBuilder.builder().id(2L).name("skol").brand("ambev").build().toBeerDTO();
        BeerDTO beerDTO3 = BeerDTOBuilder.builder().id(3L).name("kaiser").brand("ambev").build().toBeerDTO();
        List<BeerDTO> expectedListBeerDTO = List.of(beerDTO1, beerDTO2, beerDTO3);
        List<Beer> expectedBeerList = expectedListBeerDTO.stream().map(beerMapper::toModel).collect(Collectors.toList());

        //when
        when(beerRepository.findAll()).thenReturn(expectedBeerList);

        //then
        List<BeerDTO> foundBeerList = beerService.listAll();

        //assert
        assertFalse(foundBeerList.isEmpty());
        assertEquals(expectedListBeerDTO, foundBeerList);

    }

    @Test
    void whenListBeerIsCalledThenReturnAnEmptyListOfBeer() {


        //when
        when(beerRepository.findAll()).thenReturn(List.of());

        //then
        List<BeerDTO> foundBeerList = beerService.listAll();

        //assert
        assertTrue(foundBeerList.isEmpty());


    }

    @Test
    void whenExclusionIsCalledWithValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException {
        // given
        BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);

        // when
        when(beerRepository.findById(expectedDeletedBeer.getId()))
                .thenReturn(Optional.of(expectedDeletedBeer));

        doNothing().when(beerRepository).deleteById(expectedDeletedBeer.getId());

        //then
        beerService.deleteById(expectedDeletedBeer.getId());

        //assert
        verify(beerRepository, times(1)).findById(expectedDeletedBeer.getId());
        verify(beerRepository, times(1)).deleteById(expectedDeletedBeer.getId());

    }

    @Test
    void whenExclusionIsCalledWithoutValidIdThenAnExceptionShouldBeThrown() {
        //given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        //when
        when(beerRepository.findById(expectedFoundBeer.getId()))
                .thenReturn(Optional.empty());

        //assert
        assertThrows(BeerNotFoundException.class, () -> beerService.deleteById(expectedFoundBeer.getId()));

    }

    @Test
    void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().quantity(50).max(100).build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToIncrement = 10;
        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;

        // then
        BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);

        assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
        assertThat(expectedQuantityAfterIncrement, lessThanOrEqualTo(expectedBeerDTO.getMax()));
    }

    @Test
    void whenIncrementIsEqualToMaxThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().quantity(50).max(100).build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToIncrement = 50;
        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;

        // then
        BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);

        assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
        assertThat(expectedQuantityAfterIncrement, lessThanOrEqualTo(expectedBeerDTO.getMax()));
    }

    @Test
    void whenIncrementIsGreaterThanMaxThenThrowException() {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().quantity(0).max(50).build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        int quantityToIncrement = 51;

        //then
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementAfterSumIsGreaterThanMaxThenThrowException() {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().quantity(50).max(100).build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        int quantityToIncrement = 51;

        //then
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementIsCalledWithInvalidIdThenThrowException() {

        //when
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());
        int quantityToIncrement = 10;

        //then
        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
    }

    @Test
    void whenDecrementIsCalledThenDecrementBeerStock() throws BeerNotFoundException, BeerStockLessThanZeroException {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().quantity(50).build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToDecrement = 5;
        int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;

        //then
        BeerDTO decrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);

        assertThat(expectedQuantityAfterDecrement, equalTo(decrementedBeerDTO.getQuantity()));
        assertThat(expectedQuantityAfterDecrement, greaterThan(0));
    }

    @Test
    void whenDecrementIsCalledToEmptyStockThenEmptyBeerStock() throws BeerNotFoundException, BeerStockLessThanZeroException {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToDecrement = expectedBeer.getQuantity();
        int expectedQuantityAfterDecrement = expectedBeer.getQuantity() - quantityToDecrement;

        //then
        BeerDTO decrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);

        assertThat(expectedQuantityAfterDecrement, equalTo(0));
        assertThat(expectedQuantityAfterDecrement, equalTo(decrementedBeerDTO.getQuantity()));
    }

    @Test
    void whenDecrementIsLowerThanZeroThenThrowException() {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().quantity(10).build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        int quantityToDecrement = 80;

        //then
        assertThrows(BeerStockLessThanZeroException.class, () -> beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement));
    }

    @Test
    void whenDecrementIsCalledWithInvalidIdThenThrowException() {
        //when
        int quantityToDecrement = 10;
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        //then
        assertThrows(BeerNotFoundException.class, () -> beerService.decrement(INVALID_BEER_ID, quantityToDecrement));
    }
}
