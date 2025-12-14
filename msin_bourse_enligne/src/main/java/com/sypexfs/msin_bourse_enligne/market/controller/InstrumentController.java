package com.sypexfs.msin_bourse_enligne.market.controller;

import com.sypexfs.msin_bourse_enligne.common.dto.ApiResponse;
import com.sypexfs.msin_bourse_enligne.market.dto.InstrumentDto;
import com.sypexfs.msin_bourse_enligne.market.dto.MarketMapper;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketInstrument;
import com.sypexfs.msin_bourse_enligne.market.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/market/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final MarketService marketService;
    private final MarketMapper marketMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InstrumentDto>>> getAllInstruments() {
        List<MarketInstrument> instruments = marketService.getAllInstruments();
        List<InstrumentDto> dtos = marketMapper.toInstrumentDtoList(instruments);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Instruments retrieved successfully"));
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<InstrumentDto>> getInstrumentBySymbol(@PathVariable String symbol) {
        return marketService.getInstrumentBySymbol(symbol)
                .map(marketMapper::toInstrumentDto)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Instrument retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<InstrumentDto>>> getActiveInstruments() {
        List<MarketInstrument> instruments = marketService.getActiveInstruments();
        List<InstrumentDto> dtos = marketMapper.toInstrumentDtoList(instruments);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Active instruments retrieved successfully"));
    }

    @GetMapping("/sector/{sector}")
    public ResponseEntity<ApiResponse<List<InstrumentDto>>> getInstrumentsBySector(@PathVariable String sector) {
        List<MarketInstrument> instruments = marketService.getInstrumentsBySector(sector);
        List<InstrumentDto> dtos = marketMapper.toInstrumentDtoList(instruments);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Instruments by sector retrieved successfully"));
    }

    @GetMapping("/sectors")
    public ResponseEntity<ApiResponse<List<String>>> getAllSectors() {
        List<String> sectors = marketService.getAllSectors();
        return ResponseEntity.ok(ApiResponse.success(sectors, "Sectors retrieved successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<InstrumentDto>>> searchInstruments(@RequestParam String keyword) {
        List<MarketInstrument> instruments = marketService.searchInstruments(keyword);
        List<InstrumentDto> dtos = marketMapper.toInstrumentDtoList(instruments);
        return ResponseEntity.ok(ApiResponse.success(dtos, "Search results retrieved successfully"));
    }
}
