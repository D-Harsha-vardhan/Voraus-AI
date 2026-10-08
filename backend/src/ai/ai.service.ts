import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class AiService {
  constructor(private prisma: PrismaService) {}

  /**
   * DOCUMENT AGENT
   * Extracts structured data from a document using NVIDIA NIM OCR model.
   */
  async processDocument(document: any, base64Data?: string) {
    console.log(`[DocumentAgent] Processing document ${document.id} of type ${document.documentType}`);
    
    let extractedData: any = {};

    if (base64Data) {
      try {
        console.log('[DocumentAgent] Calling NVIDIA OCR Model...');
        const response = await fetch('https://integrate.api.nvidia.com/v1/chat/completions', {
          method: 'POST',
          headers: {
            'Authorization': 'Bearer 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',
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          },
          body: JSON.stringify({
            model: 'meta/llama-3.2-90b-vision-instruct',
            messages: [
              {
                role: 'user',
                content: [
                  {
                    type: 'text',
                    text: 'Extract profile information from this document in valid JSON format. Based on the document type, extract these fields if available: fullName, dob, gender, nationality, passportNumber, degree, field, institution, graduationYear, cgpa, language, proficiency. Return ONLY a valid JSON object without any markdown formatting.'
                  },
                  {
                    type: 'image_url',
                    image_url: {
                      url: `data:image/jpeg;base64,${base64Data}`
                    }
                  }
                ]
              }
            ],
            max_tokens: 512,
            temperature: 0.1
          })
        });

        const result = await response.json();
        console.log('[DocumentAgent] NVIDIA API Response:', JSON.stringify(result));
        if (result.choices && result.choices.length > 0) {
          const content = result.choices[0].message.content;
          // Parse JSON from content (stripping markdown if present)
          const jsonMatch = content.match(/\{[\s\S]*\}/);
          if (jsonMatch) {
            extractedData = JSON.parse(jsonMatch[0]);
          }
        }
      } catch (error) {
        console.error('[DocumentAgent] OCR extraction failed:', error);
      }
    } else {
      // Fallback to mock if no base64 provided
      if (document.documentType === 'degree') {
        extractedData = {
          degree: "B.Tech",
          field: "Computer Science",
          institution: "ABC University",
          graduationYear: "2024",
          cgpa: "8.5"
        };
      } else if (document.documentType === 'ielts') {
        extractedData = { language: 'English', proficiency: 'C1' };
      }
    }

    // Trigger Profile Agent to update the applicant's profile
    if (document.documentType === 'degree') {
      await this.updateApplicantProfile(document.applicantId, 'education', extractedData);
    } else if (document.documentType === 'ielts' || document.documentType === 'language') {
      await this.updateApplicantProfile(document.applicantId, 'language', extractedData);
    } else if (document.documentType === 'passport') {
      await this.updateApplicantProfile(document.applicantId, 'personal', extractedData);
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
    } else if (type === 'personal') {
      await this.prisma.applicant.update({
        where: { id: applicantId },
        data: {
          fullName: data.fullName,
          dateOfBirth: data.dob,
          passportNumber: data.passportNumber,
          nationality: data.nationality
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
