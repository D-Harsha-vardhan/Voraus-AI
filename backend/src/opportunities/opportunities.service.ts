import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class OpportunitiesService {
  constructor(private prisma: PrismaService) {}

  async getAllOpportunities() {
    return this.prisma.opportunity.findMany();
  }

  async getOpportunity(id: string) {
    const opp = await this.prisma.opportunity.findUnique({ where: { id } });
    if (!opp) throw new NotFoundException('Opportunity not found');
    return opp;
  }

  async matchOpportunitiesForApplicant(applicantId: string) {
    console.log(`[OpportunityAgent] Searching Anakin API and matching for applicant ${applicantId}`);
    
    // In a real system, this would call Anakin or an external API
    // and then save the normalized results to the local DB.
    
    // Mock the external call and DB insert
    const mockedMatches = [
      {
        institution: "Technical University of Munich",
        programName: "M.Sc. Computer Science",
        location: "Munich, Germany",
        degreeType: "Master's",
        matchStatus: "High Match",
        requirements: { "Bachelor": true, "IELTS": 6.5, "German": false }
      },
      {
        institution: "RWTH Aachen University",
        programName: "M.Sc. Artificial Intelligence",
        location: "Aachen, Germany",
        degreeType: "Master's",
        matchStatus: "High Match",
        requirements: { "Bachelor": true, "IELTS": 7.0, "APS": true }
      }
    ];

    // Simulating storing the recommendations for the applicant
    // return this.prisma.applicantRecommendation.createMany({...})
    
    return mockedMatches;
  }
}
