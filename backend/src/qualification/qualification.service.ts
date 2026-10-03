import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class QualificationService {
  constructor(private prisma: PrismaService) {}

  async getQualificationStatus(applicantId: string) {
    if (!applicantId) throw new NotFoundException('Applicant ID required');
    const records = await this.prisma.qualification.findMany({
      where: { applicantId },
      orderBy: { evaluatedAt: 'desc' },
      take: 1
    });

    if (records.length === 0) {
      return { status: 'insufficient_information', message: 'Not evaluated yet' };
    }
    return records[0];
  }

  async evaluateQualification(applicantId: string) {
    console.log(`[QualificationService] Manual evaluation triggered for applicant ${applicantId}`);
    
    // Simulates triggering the Qualification Agent
    const result = await this.prisma.qualification.create({
      data: {
        applicantId,
        qualificationStatus: 'partially_qualified',
        scoreOrReasoningSummary: 'Manual evaluation: missing APS.'
      }
    });
    
    return result;
  }
}
