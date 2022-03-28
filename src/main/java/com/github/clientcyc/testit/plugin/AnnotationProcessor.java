package com.github.clientcyc.testit.plugin;

import com.github.clientcyc.testit.api.TestItApiWrapper;
import com.github.clientcyc.testit.plugin.constants.AllureAnnotation;
import com.github.clientcyc.testit.plugin.constants.TestFrameworkAnnotation;
import com.github.clientcyc.testit.plugin.constants.TestItProjects;
import com.github.clientcyc.testit.plugin.settings.AutotestDialogSettings;
import com.github.clientcyc.testit.plugin.utils.PsiUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.clientcyc.testit.api.TestItApiWrapper.getTestItApiWrapper;
import static java.lang.String.format;

public class AnnotationProcessor {

    public static void createAutoTestAnnotations(PsiMethod psiMethod, AutotestDialogSettings autotestSettings) {

        PsiAnnotation tmsLinkAnnotation =
                createTmsLinkAnnotation(psiMethod, autotestSettings.getAutotestExternalId());

        List<PsiAnnotation> lecsLinkAnnotations =
                createManualLinkAnnotations(psiMethod, TestItProjects.LECS, autotestSettings.getLecsManualTestsIds());

        List<PsiAnnotation> pmolLinkAnnotations =
                createManualLinkAnnotations(psiMethod, TestItProjects.PMOL, autotestSettings.getPmolManualTestsIds());

        List<PsiAnnotation> moleLinkAnnotations =
                createManualLinkAnnotations(psiMethod, TestItProjects.MOLE, autotestSettings.getMoleManualTestsIds());

        PsiAnnotation testAnnotation = getTestAnnotation(psiMethod);
        if (testAnnotation != null) {

            CommandProcessor.getInstance().executeCommand(
                    psiMethod.getProject(),
                    () -> ApplicationManager.getApplication().runWriteAction(() -> {

                        PsiUtils.addImport(psiMethod.getContainingFile(), AllureAnnotation.TMS_LINK);
                        PsiUtils.addImport(psiMethod.getContainingFile(), AllureAnnotation.MANUAL_LINK);
                        PsiUtils.addImport(psiMethod.getContainingFile(), TestItProjects.PROJECTS);

                        lecsLinkAnnotations.forEach(annotation -> psiMethod.getModifierList().addAfter(annotation, returnExistingOrCurrent(psiMethod, testAnnotation)));
                        pmolLinkAnnotations.forEach(annotation -> psiMethod.getModifierList().addAfter(annotation, returnExistingOrCurrent(psiMethod, testAnnotation)));
                        moleLinkAnnotations.forEach(annotation -> psiMethod.getModifierList().addAfter(annotation, returnExistingOrCurrent(psiMethod, testAnnotation)));

                        if (!psiMethod.hasAnnotation(AllureAnnotation.TMS_LINK)) {
                            psiMethod.getModifierList().addAfter(tmsLinkAnnotation, testAnnotation);
                        }
                    }),
                    "Create Autotest and add @TmsLink annotation with created UUID",
                    null
            );
        }
    }

    private static PsiAnnotation returnExistingOrCurrent(PsiMethod psiMethod, PsiAnnotation psiAnnotation) {
        if (psiMethod.hasAnnotation(AllureAnnotation.TMS_LINK)) {
            return psiMethod.getAnnotation(AllureAnnotation.TMS_LINK);
        } else return psiAnnotation;
    }

    private static List<PsiAnnotation> createManualLinkAnnotations(PsiMethod psiMethod, String testItProject, List<String> manualTestsIds) {
        TestItApiWrapper testItApiWrapper = getTestItApiWrapper(psiMethod.getProject());

        return manualTestsIds.stream()
                .distinct()
                .map(testItApiWrapper::getWorkItem)
                .filter(Objects::nonNull)
                .distinct()
                .map(workItem -> PsiUtils.createAnnotation(
                        format("@%s(project=%s, value=\"%s\" /* %s */)", AllureAnnotation.MANUAL_LINK, testItProject, workItem.getId(), workItem.getGlobalId()),
                        psiMethod
                )).collect(Collectors.toList());
    }

    private static PsiAnnotation createTmsLinkAnnotation(PsiMethod psiMethod, String autotestUuid) {
        return PsiUtils.createAnnotation(
                format("@%s(\"%s\")", AllureAnnotation.TMS_LINK, autotestUuid),
                psiMethod
        );
    }

    private static PsiAnnotation getTestAnnotation(PsiMethod psiMethod) {
        PsiAnnotation psiAnnotation = psiMethod.getAnnotation(TestFrameworkAnnotation.JUNIT5_TEST);

        if (Objects.isNull(psiAnnotation)) {
            psiAnnotation = psiMethod.getAnnotation(TestFrameworkAnnotation.TESTNG_TEST);
        }

        return psiAnnotation;
    }
}
