package com.omniguardy.backend.detection.application.model;
public record AudioReceipt(String eventId, String filename, long size, String status, double probability) {}
