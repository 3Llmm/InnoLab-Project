//package at.fhtw.ctfbackend.service;
//
//import at.fhtw.ctfbackend.services.FlagService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class FlagServiceTest {
//    private FlagService flagService;
//
//    @BeforeEach
//    void setUp() {
//        flagService = new FlagService();
//    }
//
////    @Test
////    void validateFlag_CorrectFlag_ReturnsTrue() {
////        assertTrue(flagService.validateFlag("web-101", "flag{leet_xss}"));
////    }
////
////    @Test
////    void validateFlag_WrongFlag_ReturnsFalse() {
////        assertFalse(flagService.validateFlag("web-101", "flag{wrong}"));
////    }
////
////    @Test
////    void validateFlag_UnknownChallenge_ReturnsFalse() {
////        assertFalse(flagService.validateFlag("unknown", "flag{anything}"));
////    }
//
//    @Test
//    void recordSolve_FirstTime_ReturnsTrue() {
//        boolean first = flagService.recordSolve("joe.doe", "web-101");
//        assertTrue(first);
//    }
//
//    @Test
//    void recordSolve_DuplicateSubmission_ReturnsFalse() {
//        flagService.recordSolve("joe.doe", "web-101");
//        boolean second = flagService.recordSolve("joe.doe", "web-101");
//        assertFalse(second);
//    }
//}
