package kr.ac.cbnu.tux.service;

import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.StaticPage;
import kr.ac.cbnu.tux.repository.StaticPageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class StaticPageService {

    private final StaticPageRepository staticPageRepository;


    @Transactional
    public void createAndUpdate(String name, StaticPage updated) {
        Optional<StaticPage> opPage = staticPageRepository.findByName(name);

        if (opPage.isPresent()) {
            StaticPage page = opPage.get();
            page.setBody(updated.getBody());
        }
        else {
            staticPageRepository.save(updated);
        }
    }

    public StaticPage read(String name) {
        return staticPageRepository.findByName(name).orElseThrow();
    }

}
