package com.emreuslu.techstack.backend.job.service;

import com.emreuslu.techstack.backend.job.entity.TitleAlias;
import com.emreuslu.techstack.backend.job.repository.TitleAliasRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TitleAliasClassificationTest {

    @Mock
    private TitleAliasRepository titleAliasRepository;

    @InjectMocks
    private TitleAliasService titleAliasService;

    private TitleAlias backendAlias;
    private TitleAlias frontendAlias;

    @BeforeEach
    void setUp() {
        backendAlias = TitleAlias.builder()
                .id(1L)
                .rawTitlePattern("Backend Engineer")
                .normalizedTitle("Backend Engineer")
                .roleFamily("BACKEND")
                .roleSubfamily("BACKEND")
                .active(true)
                .build();

        frontendAlias = TitleAlias.builder()
                .id(2L)
                .rawTitlePattern("Frontend Engineer")
                .normalizedTitle("Frontend Engineer")
                .roleFamily("FRONTEND")
                .roleSubfamily("FRONTEND")
                .active(true)
                .build();
    }

    @Test
    void getActiveTitleAliases_returnsList() {
        when(titleAliasRepository.findByActiveTrue())
                .thenReturn(List.of(backendAlias, frontendAlias));

        List<TitleAlias> result = titleAliasService.getActiveTitleAliases();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(a -> a.getRoleFamily().equals("BACKEND")));
        assertTrue(result.stream().anyMatch(a -> a.getRoleFamily().equals("FRONTEND")));
        verify(titleAliasRepository, times(1)).findByActiveTrue();
    }

    @Test
    void getTitleAliasesByRoleFamily_filtersByFamily() {
        when(titleAliasRepository.findByRoleFamily("BACKEND"))
                .thenReturn(List.of(backendAlias));

        List<TitleAlias> result = titleAliasService.getTitleAliasesByRoleFamily("BACKEND");

        assertEquals(1, result.size());
        assertEquals("Backend Engineer", result.getFirst().getNormalizedTitle());
        verify(titleAliasRepository, times(1)).findByRoleFamily("BACKEND");
    }

    @Test
    void createTitleAlias_persistsNewAlias() {
        TitleAlias newAlias = TitleAlias.builder()
                .rawTitlePattern("DevOps Engineer")
                .normalizedTitle("DevOps Engineer")
                .roleFamily("DEVOPS")
                .roleSubfamily("DEVOPS")
                .active(true)
                .build();

        when(titleAliasRepository.save(any(TitleAlias.class))).thenReturn(newAlias);

        TitleAlias result = titleAliasService.createTitleAlias(
                "DevOps Engineer",
                "DevOps Engineer",
                "DEVOPS",
                "DEVOPS"
        );

        assertNotNull(result);
        assertEquals("DEVOPS", result.getRoleFamily());
        assertTrue(result.isActive());
        verify(titleAliasRepository, times(1)).save(any(TitleAlias.class));
    }
}

