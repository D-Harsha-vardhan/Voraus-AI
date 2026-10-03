import { Test, TestingModule } from '@nestjs/testing';
import { ApplicantService } from './applicant.service.js';

describe('ApplicantService', () => {
  let service: ApplicantService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ApplicantService],
    }).compile();

    service = module.get<ApplicantService>(ApplicantService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
