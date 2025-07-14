package com.shop.respawn;

import com.shop.respawn.service.MyService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class MyBean {

    private final MyService myService;

    @EventListener(ApplicationReadyEvent.class)
    public void initAfterStartup() {
        myService.initData();
    }
}