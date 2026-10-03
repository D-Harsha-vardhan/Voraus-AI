import { Controller, Get, Post, Req } from '@nestjs/common';
import { QualificationService } from './qualification.service';

@Controller('qualification')
export class QualificationController {
  constructor(private readonly qualificationService: QualificationService) {}

  @Get()
  async getQualificationStatus(@Req() req: any) {
    const applicantId = req.headers['x-applicant-id'];
    return this.qualificationService.getQualificationStatus(applicantId);
  }

  @Post('evaluate')
  async evaluateQualification(@Req() req: any) {
    const applicantId = req.headers['x-applicant-id'];
    // In production, this would trigger the AI Service's Qualification Agent
    return this.qualificationService.evaluateQualification(applicantId);
  }
}
