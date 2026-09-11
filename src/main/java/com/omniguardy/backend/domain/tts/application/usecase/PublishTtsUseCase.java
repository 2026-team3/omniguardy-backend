package com.omniguardy.backend.domain.tts.application.usecase;

import com.omniguardy.backend.domain.tts.application.model.PublishTtsCommand;
import com.omniguardy.backend.domain.tts.application.port.out.TtsDeliveryPort;
import com.omniguardy.backend.global.exception.CustomException;
import com.omniguardy.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublishTtsUseCase {
    private final TtsDeliveryPort deliveryPort;

    public void publish(PublishTtsCommand command) {
        if (command.message().length() > 100) throw new CustomException(ErrorCode.TTS_MESSAGE_TOO_LONG);
        deliveryPort.synthesizeAndPublish(command.message());
    }
}
