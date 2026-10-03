import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class ApplicantService {
  constructor(private prisma: PrismaService) {}

  async getProfile(applicantId: string) {
    if (!applicantId) throw new NotFoundException('Applicant ID required');
    
    const applicant = await this.prisma.applicant.findUnique({
      where: { id: applicantId },
      include: {
        education: true,
        employment: true,
        skills: true,
        languages: true,
        documents: true,
        qualifications: true,
      }
    });

    if (!applicant) {
      throw new NotFoundException('Applicant not found');
    }

    return applicant;
  }

  async updateProfile(applicantId: string, updateData: any) {
    if (!applicantId) throw new NotFoundException('Applicant ID required');

    return this.prisma.applicant.update({
      where: { id: applicantId },
      data: updateData,
    });
  }
}
