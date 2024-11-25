package com.bht.ludonova.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import com.bht.ludonova.dto.gameInstance.GameInstanceResponseDTO;
import com.bht.ludonova.model.GameInstance;

@Component
@RequiredArgsConstructor
public class GameInstanceMapper {

    public GameInstanceResponseDTO toDTO(GameInstance instance) {
        if (instance == null) {
            return null;
        }

        GameInstanceResponseDTO dto = new GameInstanceResponseDTO();
        dto.setId(instance.getId());
        dto.setGameId(instance.getGame().getId());
        dto.setGameTitle(instance.getGame().getTitle());
        dto.setBackgroundImage(instance.getGame().getBackgroundImage());
        dto.setStatus(instance.getStatus());
        dto.setProgressPercentage(instance.getProgressPercentage());
        dto.setPlayTime(instance.getPlayTime());
        dto.setNotes(instance.getNotes());
        dto.setLastPlayed(instance.getLastPlayed());
        dto.setAddedAt(instance.getAddedAt());
        dto.setGenres(instance.getGame().getGenres());
        return dto;
    }

    public List<GameInstanceResponseDTO> toDTOList(List<GameInstance> instances) {
        if (instances == null) {
            return Collections.emptyList();
        }
        return instances.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<GameInstanceResponseDTO> toDTOPage(Page<GameInstance> page) {
        if (page == null) {
            return Page.empty();
        }
        return page.map(this::toDTO);
    }
}