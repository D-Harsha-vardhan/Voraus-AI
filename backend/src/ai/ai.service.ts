import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class AiService {
  constructor(private prisma: PrismaService) {}

  /**
   * DOCUMENT AGENT
   * Simulates extracting structured data from a document.
   */
  async processDocument(document: any) {
    console.log(`[DocumentAgent] Processing document ${document.id} of type ${document.documentType}`);
    
    // In production, we'd fetch the file from storage and send to an LLM or OCR service.
    // For now, we mock the extraction result based on the document type.
    let extractedData = {};

    switch (document.documentType) {
      case 'degree':
        extractedData = {
          degree: "B.Tech",
          field: "Computer Science",
          institution: "ABC University",
          graduationYear: "2024",
          cgpa: "8.5"
        };
        // Trigger Profile Agent to update the applicant's profile
        await this.updateApplicantProfile(document.applicantId, 'education', extractedData);
        break;
      case 'ielts':
        extractedData = {
          overallScore: "7.5",
          reading: "8.0",
          listening: "8.0",
          speaking: "7.0",
          writing: "7.0",
          testDate: "2023-10-15"
        };
        await this.updateApplicantProfile(document.applicantId, 'language', {
          language: 'English',
          proficiency: 'C1'
        });
        break;
      default:
        extractedData = { summary: "Document processed successfully" };
    }

    return extractedData;
  }

  /**
   * PROFILE AGENT
   * Merges extracted information into the structured applicant profile.
   */
  async updateApplicantProfile(applicantId: string, type: string, data: any) {
    console.log(`[ProfileAgent] Updating profile for applicant ${applicantId} with ${type} data`);

    if (type === 'education') {
      await this.prisma.education.create({
        data: {
          applicantId,
          degree: data.degree,
          fieldOfStudy: data.field,
          institution: data.institution,
          cgpa: data.cgpa,
          verificationStatus: 'verified'
        }
      });
    } else if (type === 'language') {
      await this.prisma.language.create({
        data: {
          applicantId,
          language: data.language,
          proficiency: data.proficiency,
          verificationStatus: 'verified'
        }
      });
    }
    
    // Calculate new profile completion percentage
    await this.prisma.applicant.update({
      where: { id: applicantId },
      data: { profileCompletion: { increment: 10 } }
    });

    // Run Qualification Agent asynchronously
    this.runQualificationCheck(applicantId).catch(console.error);
  }

  /**
   * QUALIFICATION AGENT
   * Evaluates the applicant against predefined requirements.
   */
  async runQualificationCheck(applicantId: string) {
    console.log(`[QualificationAgent] Running qualification check for applicant ${applicantId}`);
    
    // Mock logic: if they have education and language, they are partially qualified
    await this.prisma.qualification.create({
      data: {
        applicantId,
        qualificationStatus: 'partially_qualified',
        scoreOrReasoningSummary: 'Has degree and english, missing German and APS.'
      }
    });

    // Trigger Journey Agent
    await this.runJourneyOrchestrator(applicantId);
  }

  /**
   * JOURNEY AGENT (ORCHESTRATOR)
   * Decides what should happen next based on current state.
   */
  async runJourneyOrchestrator(applicantId: string) {
    console.log(`[JourneyAgent] Running orchestrator for applicant ${applicantId}`);
    
    // Creates an agent run record
    await this.prisma.agentRun.create({
      data: {
        applicantId,
        agentName: 'JourneyAgent',
        status: 'completed',
        outputSummary: 'Recommended next step: Upload APS certificate'
      }
    });
  }
}
