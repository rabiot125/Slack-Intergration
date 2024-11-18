package com.example.slackpoc.service.impl;

import com.example.slackpoc.service.SlackService;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.element.BlockElements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
@Service
public class SlackServiceImpl implements SlackService {
    @Value("${slack.bot.token}")
    private String botToken;
    @Override
    public void sendFormToUser(String userId) throws Exception {
        Slack slack = Slack.getInstance();
        MethodsClient methods = slack.methods(botToken);

        List<LayoutBlock> blocks = Arrays.asList(
                Blocks.header(header -> header.text(
                        BlockCompositions.plainText("IT Request Form")
                )),
                Blocks.section(section -> section.text(
                        BlockCompositions.markdownText("Request help from IT Support:")
                )),
                Blocks.input(input -> input
                        .blockId("support_block")
                        .label(BlockCompositions.plainText("Type of Support"))
                        .element(BlockElements.staticSelect(select -> select
                                .actionId("support_select")
                                .options(Arrays.asList(
                                        BlockCompositions.option(option -> option.text(
                                                BlockCompositions.plainText("Hardware Support")).value("hardware")),
                                        BlockCompositions.option(option -> option.text(
                                                BlockCompositions.plainText("Software Support")).value("software")),
                                        BlockCompositions.option(option -> option.text(
                                                BlockCompositions.plainText("Other Request")).value("others"))
                                ))
                        ))
                ),
                Blocks.input(input -> input
                        .blockId("feedback_block")
                        .label(BlockCompositions.plainText("Your feedback"))
                        .element(BlockElements.plainTextInput(textInput -> textInput
                                .actionId("feedback_input")
                                .multiline(true)
                        ))
                ),
                Blocks.input(input -> input
                        .blockId("date_required_block")
                        .label(BlockCompositions.plainText("Date required"))
                        .element(BlockElements.datePicker(datePicker -> datePicker
                                .actionId("date_picker")
                                .placeholder(BlockCompositions.plainText("Select a date"))
                        ))
                ),
                Blocks.input(input -> input
                        .blockId("priority_block")
                        .label(BlockCompositions.plainText("Priority"))
                        .element(BlockElements.staticSelect(select -> select
                                .actionId("priority_select")
                                .options(Arrays.asList(
                                        BlockCompositions.option(option -> option.text(
                                                BlockCompositions.plainText("High")).value("high")),
                                        BlockCompositions.option(option -> option.text(
                                                BlockCompositions.plainText("Medium")).value("medium")),
                                        BlockCompositions.option(option -> option.text(
                                                BlockCompositions.plainText("Low")).value("low"))
                                ))
                        ))
                ),
                Blocks.actions(actions -> actions
                        .elements(Arrays.asList(
                                BlockElements.button(button -> button
                                        .text(BlockCompositions.plainText("Submit"))
                                        .actionId("submit_form")
                                        .style("primary")
                                )
                        ))
                )
        );

        ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .channel(userId)
                .blocks(blocks)
                .text("Please fill out this feedback form")
                .build();

        ChatPostMessageResponse response = methods.chatPostMessage(request);

        if (response.isOk()) {
            System.out.println("Form sent successfully");
        } else {
            System.err.println("Error sending form: " + response.getError());
        }
    }

}
